package cn.edu.fudan.violation.dao;

import cn.edu.fudan.violation.domain.dbo.IssueAnalyzer;
import cn.edu.fudan.violation.domain.dbo.RawIssue;
import cn.edu.fudan.violation.util.RawIssueParseUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonSerializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author beethoven
 * @author PJH
 */
@Repository
@Slf4j
public class IssueAnalyzerDao {

    private static final String collectionName = "raw_issue_cache";
    private MongoTemplate mongoTemplate;

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertIssueAnalyzer(IssueAnalyzer issueAnalyzer) {
        String repoUuid = issueAnalyzer.getRepoUuid();
        String commitId = issueAnalyzer.getCommitId();
        String tool = issueAnalyzer.getTool();
        if (!cached(repoUuid, commitId, tool)) {
            //由于mongoDB有单个文档16MB的限制 所以当缺陷条数超过1000时，需要分割
            log.info("start insert analyzer");
            if(insertIssueAnalyzerPartition(issueAnalyzer, 1000, 0) != -1){
                log.info("repoUuid:{},commitId:{} issueAnalyzer insert success!", repoUuid, commitId);
            } else {
                log.error("repoUuid:{},commitId:{} issueAnalyzer insert failed!", repoUuid, commitId);
            }
        }
    }

    private int insertIssueAnalyzerPartition(IssueAnalyzer issueAnalyzer, int partitionNum, int beginSharding){
        String repoUuid = issueAnalyzer.getRepoUuid();
        String commitId = issueAnalyzer.getCommitId();
        String tool = issueAnalyzer.getTool();
        List<RawIssue> rawIssueList = issueAnalyzer.getRawIssueList();
        if(partitionNum == 0){
            log.info("repoUuid:{},commitId:{},partitionNum:{},beginSharding:{} partitionNum is less than 0", repoUuid, commitId, partitionNum, beginSharding);
            return -1;
        }
        log.info("repoUuid:{},commitId:{},partitionNum:{},beginSharding:{} begin partition", repoUuid, commitId, partitionNum, beginSharding);
        int insertedNumInPartition = 0;
        if(rawIssueList.isEmpty()){
            issueAnalyzer.setSharding(0);
            mongoTemplate.insert(issueAnalyzer, collectionName);
            return 0;
        }
        List<List<RawIssue>> rawIssuesOfPartition = Lists.partition(rawIssueList, partitionNum);
        for (int i = 0; i < rawIssuesOfPartition.size(); i++) {
            final List<RawIssue> tempRawIssueList = rawIssuesOfPartition.get(i);
            IssueAnalyzer temp = IssueAnalyzer.initIssueAnalyze(repoUuid,commitId,tool);
            temp.setInvokeResult(IssueAnalyzer.InvokeResult.SUCCESS.getStatus());
            temp.setRawIssueNum(tempRawIssueList.size());
            temp.setAnalyzeResult(RawIssueParseUtil.rawIssues2JSON(tempRawIssueList));
            temp.setSharding(i + beginSharding + insertedNumInPartition);
            temp.setRawIssueList(tempRawIssueList);
            temp.setIsTotalScan(issueAnalyzer.getIsTotalScan());
            try {
                mongoTemplate.insert(temp, collectionName);
            } catch (BsonSerializationException e){
                log.info("repoUuid:{},commitId:{},partitionNum:{},beginSharding:{} over 16MB, begin next partition", repoUuid, commitId, partitionNum, beginSharding);
                int insertNum = insertIssueAnalyzerPartition(temp, partitionNum/2, i + beginSharding);
                if(insertNum < 0){
                    //底层依旧插入失败
                    return -1;
                }
                //-1是为了去掉原有的一个sharding
                insertedNumInPartition += (insertNum - 1);
            } catch (Exception e){
                log.error("insert issueAnalyzer failed" + e.getMessage());
                return -1;
            }
        }
        insertedNumInPartition += rawIssuesOfPartition.size();
        return insertedNumInPartition;
    }

    public JSONObject getAnalyzeResultByRepoUuidCommitIdTool(String repoUuid, String commitId, String tool) {
        Query query = Query.query(createCriteria(repoUuid, commitId, tool));
        List<IssueAnalyzer> issueAnalyzers = mongoTemplate.find(query, IssueAnalyzer.class, collectionName);
        if (!issueAnalyzers.isEmpty()) {
            List<RawIssue> rawIssueList = new ArrayList<>();
            issueAnalyzers.forEach(issueAnalyzer -> {
                if(issueAnalyzer.getInvokeResult() == 1){
                    rawIssueList.addAll(RawIssueParseUtil.json2RawIssues(issueAnalyzer.getAnalyzeResult()));
                }
            });
            return RawIssueParseUtil.rawIssues2JSON(rawIssueList);
        }
        return null;
    }

    public int getCacheCount(String repoUuid, String toolName){
        return (int) mongoTemplate.count(Query.query(createRepoCriteria(repoUuid, toolName)), IssueAnalyzer.class, collectionName);
    }

    public boolean deleteRepo(String repoUuid, String toolName) {
        try {
            mongoTemplate.remove(Query.query(createRepoCriteria(repoUuid, toolName)), IssueAnalyzer.class, collectionName);
            return true;
        } catch (Exception e) {
            log.error("delete cache repo:{} tool:{} failed",repoUuid, toolName);
            return false;
        }
    }

    public boolean cached(String repoUuid, String commit, String toolName) {
//        return issueAnalyzerMapper.cached(repoUuid, commit, toolName) > 0;
        return mongoTemplate.count(Query.query(createCriteriaForCached(repoUuid, commit, toolName)), IssueAnalyzer.class, collectionName) > 0;
    }

    public Criteria createCriteriaForCached(String repoUuid, String commitId, String toolName) {
        return Criteria.where("repoUuid").is(repoUuid)
                .and("commitId").is(commitId)
                .and("tool").is(toolName)
                .and("invokeResult").is(1);
    }

    public Criteria createCriteria(String repoUuid, String commitId, String toolName) {
        return Criteria.where("repoUuid").is(repoUuid)
                .and("commitId").is(commitId)
                .and("tool").is(toolName);
    }

    private CriteriaDefinition createRepoCriteria(String repoUuid, String toolName) {
        return Criteria.where("repoUuid").is(repoUuid)
                .and("tool").is(toolName);
    }

    public Integer getOneCommitTotalIssueNum(String repoUuid, String commitId, String toolName) {
//        return issueAnalyzerMapper.getOneCommitTotalIssueNum(repoUuid, commitId);
        Query query = new Query();
        query.fields().include("rawIssueNum");
        query.addCriteria(createCriteria(repoUuid, commitId, toolName));
        return mongoTemplate.find(query, IssueAnalyzer.class, collectionName).stream().map(IssueAnalyzer::getRawIssueNum).reduce(Integer::sum).orElse(0);
    }

    public void updateTotalIssueNum(String repoUuid, String commitId, String tool, int num) {
//        issueAnalyzerMapper.updateTotalIssueNum(repoUuid, commitId, num);
        Update update = Update.update("rawIssueNum", num);
        Query query = Query.query(createCriteria(repoUuid, commitId, tool));
        mongoTemplate.updateFirst(query, update, IssueAnalyzer.class, collectionName);
    }

    public Integer getInvokeResult(String repoUuid, String commitId, String toolName) {
        Query query = new Query();
        query.fields().include("invokeResult");
        query.addCriteria(createCriteria(repoUuid, commitId, toolName));
        return Objects.requireNonNull(mongoTemplate.findOne(query, IssueAnalyzer.class, collectionName)).getInvokeResult();
//        return issueAnalyzerMapper.getInvokeResult(repoUuid, commitId, toolName);
    }

    public Integer getIsTotalScan(String repoUuid, String commitId, String toolName) {
        Query query = new Query();
        query.fields().include("isTotalScan");
        query.addCriteria(createCriteria(repoUuid, commitId, toolName));
        return Objects.requireNonNull(mongoTemplate.findOne(query, IssueAnalyzer.class, collectionName)).getIsTotalScan();
    }

}
