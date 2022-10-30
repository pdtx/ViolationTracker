package cn.edu.fudan.violation.util;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author fancying
 * create: 2019-06-05 17:16:
 **/
@SuppressWarnings("Duplicates")
@Slf4j
public class JGitHelper {

    protected static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    protected String REPO_PATH;

    protected Repository repository;

    protected RevWalk revWalk;

    protected Git git;

    public Repository getRepository() {
        return repository;
    }

    protected static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected static final long TO_MILLISECOND = 1000L;

    protected static final int COMMIT_SIZE = 1000;

    public JGitHelper(String repoPath) {
        REPO_PATH = repoPath;
        String gitDir = IS_WINDOWS ? repoPath + "\\.git" : repoPath + "/.git";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            repository = builder.setGitDir(new File(gitDir))
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
            git = new Git(repository);
            revWalk = new RevWalk(repository);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
//        log.info(" now git branch :"+git.branchRename().);
    }

    public String getRepoPath() {
        return REPO_PATH;
    }

    /**
     * 切换到指定 commit 版本
     *
     * @param commit 指定的版本
     * @return true : 切换成功 ， false : 切换失败
     */
    public boolean checkout(String commit) {

        try {
            initCheckOut(commit);
            return true;
        } catch (Exception e) {
            log.error("JGitHelper checkout error:{} ", e.getMessage());
            log.error("begin second checkout {}", commit);
            try {
                // clean for checkOut
                secondCheckOut(commit);
                return true;
            } catch (Exception e2) {
                log.error("second checkout error:{} ", e2.getMessage());
            }

        }
        return false;
    }

    private void initCheckOut(String commit) throws IOException, GitAPIException {
        // 不加上这一句  有新增和删除的情况还是会成功
        //git.reset().setMode(ResetCommand.ResetType.HARD).call();

        if (commit == null) {
            commit = repository.getBranch();
        }
        CheckoutCommand checkoutCommand = git.checkout();
        checkoutCommand.setName(commit).call();
    }

    private void secondCheckOut(String commit) throws GitAPIException, IOException {
        // check index.lock
        File lock = new File(IS_WINDOWS ? REPO_PATH + "\\.git\\index.lock" : REPO_PATH + "/.git/index.lock");
        if (lock.exists() && lock.delete()) {
            log.error("repo[{}] index.lock exists, deleted! ", REPO_PATH);
        }

        //git.reset().setMode(ResetCommand.ResetType.HARD).call();

        // check modify
        git.add().addFilepattern(".").call();
        git.stashCreate().call();

        initCheckOut(commit);
    }

    /**
     * 获取这次 commit 开发者的邮件地址
     *
     * @param commit 查询版本
     * @return 当前版本提交者的 email 地址
     */
    public String getAuthorEmailAddress(String commit) {
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            return revCommit.getAuthorIdent().getEmailAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取该次commit的提交信息
     *
     * @param commit 查询版本
     * @return 当前版本的提交信息明细
     */
    public String getCommitMessage(String commit) {
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            return revCommit.getShortMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前 commit 的提交者
     *
     * @param commit 查询版本号
     * @return 提交者
     */
    public String getAuthorName(String commit) {
        String authorName = null;
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            authorName = revCommit.getAuthorIdent().getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authorName;
    }

    /**
     * 获取当前 commit 的提交日期
     *
     * @param commit 查询版本号
     * @return {@link Date 提交日期}
     */
    public Date getCommitDateTime(String commit) {
        return string2Date(getCommitTime(commit));
    }

    /**
     * 获取当前 commit 的提交时间
     *
     * @param commit 查询版本号
     * @return {@link String 提交时间}
     */
    public String getCommitTime(String commit) {
        String time = null;
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            int t = revCommit.getCommitTime();
            long timestamp = Long.parseLong(String.valueOf(t)) * TO_MILLISECOND;
            Date date = new Date(timestamp);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR_OF_DAY, -8);
            time = new SimpleDateFormat(FORMAT).format(calendar.getTime());
        } catch (Exception e) {
            log.error("error in revWalk.parseCommit(ObjectId.fromString({})):", commit);
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return time;
    }


    private Date string2Date(String dateStr) {

        if (dateStr == null) {
            log.error("date is null");
            return null;
        }

        try {
            return new SimpleDateFormat(FORMAT).parse(dateStr);
        } catch (ParseException e) {
            log.error("ParseException:" + e.getMessage());
        }

        return null;
    }

    /**
     * 获取当前版本的提交时间
     *
     * @param version 查询版本
     * @return {@link Long 提交时间的毫秒级表示}
     */
    public Long getLongCommitTime(String version) {
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(version));
            return revCommit.getCommitTime() * TO_MILLISECOND;
        } catch (Exception e) {
            log.error("error in getLongCommitTime:" + e.getMessage());
            return 0L;
        }
    }


    /**
     * @param branch, date is timestamp that unit is s
     * @return commitid
     * @Description get the close
     */
    public String getToScanCommit(String branch, int date) {
        checkout(branch);
        String resCommit = null;
        int latest = 0;
        try {
            Iterable<RevCommit> commits = git.log().call();
            Iterator<RevCommit> iterator = commits.iterator();
            while (iterator.hasNext()) {
                RevCommit oneCommit = iterator.next();
                int thisCommitTime = oneCommit.getCommitTime();
                if (thisCommitTime < date) {
                    if (thisCommitTime > latest) {
                        resCommit = oneCommit.getName();
                        latest = thisCommitTime;
                    }
                }
            }
        } catch (GitAPIException e) {
            log.error(e.getMessage());
        }
        log.info("to scan time : " + getCommitDateTime(resCommit));
        return resCommit;

    }

    /**
     * @param branch 查询分支
     * @return commitid
     * @Description get the latest commit of the branch
     * @author shaoxi
     */
    public String getLatestCommitByBranch(String branch) {
        checkout(branch);
        String latestCommit = null;
        int latest = 0;
        try {
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit oneCommit : commits) {
                if (oneCommit.getCommitTime() > latest) {
                    latestCommit = oneCommit.getName();
                    latest = oneCommit.getCommitTime();
                }

            }

        } catch (GitAPIException e) {
            log.error(e.getMessage());
        }
        return latestCommit;

    }

    /**
     * 判断 commit2 是否为 commit1 的 pre parent commit
     *
     * @param commit1 commit1
     * @param commit2 commit2
     * @return true / false
     */
    public boolean isParent(String commit1, String commit2) throws IOException {
        List<String> commitParents = getAllCommitParents(commit1);
        return commitParents.contains(commit2);
    }

    /**
     * 如 0 -》 1 》 2  那 getAllCommitParents（2） = {2，1，0}
     * 得到这个commit所有的parent（包含此commit）
     **/
    public List<String> getAllCommitParents(String commit) {
        List<String> parentCommitList = new ArrayList<>();
        Queue<String> parentCommitQueue = new LinkedList<>();
        parentCommitQueue.offer(commit);
        while (!parentCommitQueue.isEmpty()) {
            String indexCommit = parentCommitQueue.poll();
            parentCommitList.add(indexCommit);
            RevCommit[] parents = getRevCommit(indexCommit).getParents();
            for (RevCommit parent : parents) {
                if (!parentCommitList.contains(parent.getName()) && !parentCommitQueue.contains(parent.getName())) {
                    parentCommitQueue.offer(parent.getName());
                }
            }
        }
        return parentCommitList;
    }

    /**
     * 获取当前commit的 父节点
     *
     * @param commit 查询 commit
     * @return 父节点数组
     */
    public String[] getCommitParents(String commit) {
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            RevCommit[] parentCommits = revCommit.getParents();
            String[] result = new String[parentCommits.length];
            for (int i = 0; i < parentCommits.length; i++) {
                result[i] = parentCommits[i].getName();
            }
            return result;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new String[0];
    }

    /**
     * 获取当前 commit 相较于上一个版本的 diff 信息
     *
     * @param commit 查询版本
     * @return key : parentCommit, value : {@link List<DiffEntry> 修改信息列表}
     */
    public Map<String, List<DiffEntry>> getMappedFileList(String commit) {
        Map<String, List<DiffEntry>> result = new HashMap<>(8);
        try {
            RevCommit currCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            RevCommit[] parentCommits = currCommit.getParents();
            for (RevCommit p : parentCommits) {
                RevCommit parentCommit = revWalk.parseCommit(ObjectId.fromString(p.getName()));
                try (ObjectReader reader = git.getRepository().newObjectReader()) {
                    CanonicalTreeParser currTreeIter = new CanonicalTreeParser();
                    currTreeIter.reset(reader, currCommit.getTree().getId());

                    CanonicalTreeParser parentTreeIter = new CanonicalTreeParser();
                    parentTreeIter.reset(reader, parentCommit.getTree().getId());
                    DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                    diffFormatter.setRepository(git.getRepository());
                    List<DiffEntry> entries = diffFormatter.scan(currTreeIter, parentTreeIter);
                    result.put(parentCommit.getName(), entries);
                }
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据两个commit id 来diff两个
     *
     * @param preCommitId      前一个版本的commit id
     * @param commitId         当前版本的commit id
     * @param curFileToPreFile curFileToPreFile
     * @param preFileToCurFile preFileToCurFile
     * @return add : ,a delete: a,   change a,a   英文逗号 ， 区分 add delete change
     */
    public List<String> getDiffFilePair(String preCommitId, String
            commitId, Map<String, String> preFileToCurFile, Map<String, String> curFileToPreFile) {
        //new result list
        List<String> result = new ArrayList<>();
        Map<String, List<DiffEntry>> changeDiffEntryList = new HashMap<>(32);
        //init git diff
        CanonicalTreeParser oldTreeDiff = new CanonicalTreeParser();
        CanonicalTreeParser newTreeDiff = new CanonicalTreeParser();
        try (ObjectReader reader = repository.newObjectReader()) {
            //get diff tree
            oldTreeDiff.reset(reader, repository.resolve(preCommitId + "^{tree}"));
            newTreeDiff.reset(reader, repository.resolve(commitId + "^{tree}"));
            //call git diff command
            List<DiffEntry> diffs = getDiffEntry(getRevCommit(preCommitId), getRevCommit(commitId), 60);
            String separation = ",";
            for (DiffEntry diff : diffs) {
                switch (diff.getChangeType()) {
                    case ADD:
                        result.add(separation + diff.getNewPath());
                        break;
                    case DELETE:
                        result.add(diff.getOldPath() + separation);
                        break;
                    default:
                        List<DiffEntry> changeDiffEntry = changeDiffEntryList.getOrDefault(diff.getOldPath(), new ArrayList<>());
                        changeDiffEntry.add(diff);
                        changeDiffEntryList.put(diff.getOldPath(), changeDiffEntry);
                }
            }
            //针对一对多的情况进行处理
            for (Map.Entry<String, List<DiffEntry>> stringListEntry : changeDiffEntryList.entrySet()) {
                List<DiffEntry> diffEntries = stringListEntry.getValue();
                boolean hasRename = false;
                for (int i = 0; i < diffEntries.size(); i++) {
                    if(diffEntries.get(i).getChangeType().equals(DiffEntry.ChangeType.RENAME)){
                        DiffEntry diff = diffEntries.get(i);
                        result.add(diff.getOldPath() + separation + diff.getNewPath());
                        preFileToCurFile.put(diff.getOldPath(), diff.getNewPath());
                        curFileToPreFile.put(diff.getNewPath(), diff.getOldPath());
                        diffEntries.remove(i);
                        hasRename = true;
                        break;
                    }
                }
                if(!hasRename){
                    DiffEntry diff = diffEntries.get(0);
                    result.add(diff.getOldPath() + separation + diff.getNewPath());
                    preFileToCurFile.put(diff.getOldPath(), diff.getNewPath());
                    curFileToPreFile.put(diff.getNewPath(), diff.getOldPath());
                    diffEntries.remove(0);
                }
                diffEntries.forEach(diff -> result.add(separation + diff.getNewPath()));
            }
        } catch (Exception e) {
            log.error("get diff file failed!pre commit is: {}, cur commit is: {}", preCommitId, commitId);
        }
        return result;
    }

  public static void main(String[] args) {
    JGitHelper jGitHelper = new JGitHelper("E:\\repository\\t-repo\\cicada");
    DiffFile diffFile = jGitHelper.getDiffFilePair("1db5b88be2ac0a760070afc81b4fa51548568e9f","556022b83d314fd47cd4bf2eb1c44162e0d6e426");

    System.out.println(diffFile);
  }

    /**
     * 根据两个commit id 来diff两个
     *
     * @param preCommitId 前一个版本的commit id
     * @param commitId    当前版本的commit id
     * @return add : ,a delete: a,   change a,a   英文逗号 ， 区分 add delete change
     */
    public DiffFile getDiffFilePair(String preCommitId, String commitId) {

        List<String> addFiles = new ArrayList<>(8);
        List<String> deleteFiles = new ArrayList<>(8);
        Map<String, List<DiffEntry>> changeDiffEntryList = new HashMap<>(32);
        Map<String, String> changeFiles = new HashMap<>(32);

        //init git diff
        CanonicalTreeParser oldTreeDiff = new CanonicalTreeParser();
        CanonicalTreeParser newTreeDiff = new CanonicalTreeParser();
        try (ObjectReader reader = repository.newObjectReader()) {
            //get diff tree
            oldTreeDiff.reset(reader, repository.resolve(preCommitId + "^{tree}"));
            newTreeDiff.reset(reader, repository.resolve(commitId + "^{tree}"));
            //call git diff command
            List<DiffEntry> diffs = getDiffEntry(getRevCommit(preCommitId), getRevCommit(commitId), 60);
            for (DiffEntry diff : diffs) {
                switch (diff.getChangeType()) {
                    case ADD:
                        addFiles.add(diff.getNewPath());
                        break;
                    case DELETE:
                        deleteFiles.add(diff.getOldPath());
                        break;
                    default:
                        List<DiffEntry> changeDiffEntry = changeDiffEntryList.getOrDefault(diff.getOldPath(), new ArrayList<>());
                        changeDiffEntry.add(diff);
                        changeDiffEntryList.put(diff.getOldPath(), changeDiffEntry);
                }
            }
            //针对一对多的情况进行处理
            for (Map.Entry<String, List<DiffEntry>> stringListEntry : changeDiffEntryList.entrySet()) {
                String oldPath = stringListEntry.getKey();
                List<DiffEntry> diffEntries = stringListEntry.getValue();
                boolean hasRename = false;
                for (int i = 0; i < diffEntries.size(); i++) {
                    if(diffEntries.get(i).getChangeType().equals(DiffEntry.ChangeType.RENAME)){
                        changeFiles.put(oldPath, diffEntries.get(i).getNewPath());
                        diffEntries.remove(i);
                        hasRename = true;
                        break;
                    }
                }
                if(!hasRename){
                    changeFiles.put(oldPath, diffEntries.get(0).getNewPath());
                    diffEntries.remove(0);
                }
                addFiles.addAll(diffEntries.stream().map(DiffEntry::getNewPath).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error("get diff file failed!pre commit is: {}, cur commit is: {}", preCommitId, commitId);
        }
        return new DiffFile(addFiles, deleteFiles, changeFiles);
    }




    /**
     * 根据两个commit id来获得两个commit的差异行
     *
     * @param preCommitId  前一个版本的commit id
     * @param commitId     当前版本的commit id
     * @param ignoreConfig 是否忽视一些配置文件的变化，如.git/.mvn/.idea等等，现在简单实现，后续可能要改写
     * @return add : Map<String, List<String>> map有两个key，分别是ADD和DELETE，
     * 记录增加行和删除行的地址及具体行数，而modify的情况则将被替代的行计入DELETE，替代的行计入ADD
     * 对于key为ADD的value中的list，每一条记录代表增加的一处实例，格式为 修改后的地址@初始行@结束行
     * 对于key为DELETE的value中的list，每一条记录代表删除的一处实例，格式为 修改前的地址@初始行@结束行
     */
    @SneakyThrows
    public Map<String, List<String>> getDiffLine(String preCommitId, String commitId, boolean ignoreConfig) {
        Map<String, List<String>> diffLineMap = new HashMap<>();
        List<String> addLines = new ArrayList<>(8);
        List<String> deleteLines = new ArrayList<>(8);

        //init git diff
        CanonicalTreeParser oldTreeDiff = new CanonicalTreeParser();
        CanonicalTreeParser newTreeDiff = new CanonicalTreeParser();
        try (ObjectReader reader = repository.newObjectReader()) {
            //get diff tree
            oldTreeDiff.reset(reader, repository.resolve(preCommitId + "^{tree}"));
            newTreeDiff.reset(reader, repository.resolve(commitId + "^{tree}"));
            //call git diff command
            List<DiffEntry> diffs = getDiffEntry(getRevCommit(preCommitId), getRevCommit(commitId), 60);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(out);
            //设置比较器为忽略空白字符对比（Ignores all whitespace）
            df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            df.setRepository(git.getRepository());
            //每一个diffEntry都是第个文件版本之间的变动差异
            for (DiffEntry diffEntry : diffs) {

                //排除一些配置文件，需要修改
                if (diffEntry.getOldPath().contains(".idea") || diffEntry.getOldPath().contains(".mvn") || diffEntry.getOldPath().contains(".git") || diffEntry.getNewPath().contains(".idea") || diffEntry.getNewPath().contains(".mvn") || diffEntry.getNewPath().contains(".git")) {
                    continue;
                }

                //打印文件差异具体内容
                df.format(diffEntry);
                //获取文件差异位置，从而统计差异的行数，如增加行数，减少行数
                FileHeader fileHeader = df.toFileHeader(diffEntry);
                List<HunkHeader> hunks = (List<HunkHeader>) fileHeader.getHunks();
                for (HunkHeader hunkHeader : hunks) {
                    EditList editList = hunkHeader.toEditList();
                    for (Edit edit : editList) {
                        switch (edit.getType()) {
                            case INSERT:
                                addLines.add(diffEntry.getNewPath().concat("@" + edit.getBeginB() + "@" + edit.getEndB()));
                                break;
                            case DELETE:
                                deleteLines.add(diffEntry.getOldPath().concat("@" + edit.getBeginA() + "@" + edit.getEndA()));
                                break;
                            case REPLACE:
                                deleteLines.add(diffEntry.getOldPath().concat("@" + edit.getBeginA() + "@" + edit.getEndA()));
                                addLines.add(diffEntry.getNewPath().concat("@" + edit.getBeginB() + "@" + edit.getEndB()));
                                break;
                            default:
                                break;
                        }
                    }
                }
                out.reset();
            }
            diffLineMap.put("ADD", addLines);
            diffLineMap.put("DELETE", deleteLines);
            log.info(String.valueOf(diffLineMap));
        } catch (Exception e) {
            log.error("get diff file failed!pre commit is: {}, cur commit is: {}", preCommitId, commitId);
        }
        return diffLineMap;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JGitHelper that = (JGitHelper) o;
        return Objects.equals(REPO_PATH, that.REPO_PATH);
    }

    @Override
    public int hashCode() {
        return Objects.hash(REPO_PATH);
    }

    @SneakyThrows
    public RevCommit getRevCommit(String commitId) {
        return revWalk.parseCommit(repository.resolve(commitId));
    }


    /**
     * 关闭 git， 释放资源
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (repository != null) {
            repository.close();
        }
    }


    //****************************************  非原子性操作  *****************************************************************************************************************************************************



    /**
     * 根据策略获取扫描列表
     *
     * @param branch        branch
     * @param beginCommit   begin commit
     * @param scannedCommit scannedCommit
     * @return commit list
     */
    public List<String> getScanCommitListByBranchAndBeginCommit(String branch, String beginCommit, Set<String> scannedCommit) {
        //checkout to the branch
        checkout(branch);

        //get the start commit time
        long start = !StringUtils.isEmpty(beginCommit) ? getLongCommitTime(beginCommit) : 0L;

        try {
            Iterable<RevCommit> commits = git.log().call();
            List<RevCommit> commitsList = new ArrayList<>();
            Set<RevCommit> deDuplication = new HashSet<>();
            Map<RevCommit, List<RevCommit>> parent2SonMap = new HashMap<>();
            for (RevCommit commit : commits) {
                if (!scannedCommit.contains(commit.getName()) && commit.getCommitTime() * TO_MILLISECOND >= start) {
                    // 待排序commits
                    commitsList.add(commit);
                    // parent2SonMap key:parent commit; value:son commit
                    RevCommit[] parents = commit.getParents();
                    for (RevCommit parent : parents) {
                        List<RevCommit> temp = parent2SonMap.getOrDefault(parent, new ArrayList<>());
                        temp.add(commit);
                        parent2SonMap.put(parent, temp);
                    }
                } else {
                    // beginCommit非起始commit时，将beginCommit以前的commit加入deDuplication
                    deDuplication.add(commit);
                }
            }
            // 将commitsList升序排列
            Collections.reverse(commitsList);
            return getNeedScanCommits(commitsList, parent2SonMap, scannedCommit, deDuplication);
        } catch (GitAPIException e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<String> getNeedScanCommits(List<RevCommit> commitsList, Map<RevCommit, List<RevCommit>> parent2SonMap, Set<String> scannedCommit, Set<RevCommit> deDuplication) {
        List<String> allCommits = new ArrayList<>();
        List<RevCommit> toBeAddedCommits = new LinkedList<>();
        Set<RevCommit> commits = new HashSet<>(commitsList);

        // commitsList中parent commit都不在commitsList中的先加入toBeAddedCommits
        for (RevCommit revCommit : commitsList) {
            boolean canBeFirst = true;
            RevCommit[] parents = revCommit.getParents();
            for (RevCommit parent : parents) {
                if (commits.contains(parent)) {
                    canBeFirst = false;
                    break;
                }
            }
            if (canBeFirst) {
                toBeAddedCommits.add(revCommit);
                deDuplication.add(revCommit);
            }
        }

        while (!toBeAddedCommits.isEmpty()) {
            // 将commit_date最早的加入结果allCommits中
            RevCommit addCommit = toBeAddedCommits.get(0);
            toBeAddedCommits.remove(0);
            allCommits.add(addCommit.getName());
            List<RevCommit> sonCommits = parent2SonMap.get(addCommit);
            if (sonCommits == null) {
                continue;
            }
            // 将该commit的son commit加入toBeAddedCommits
            sonCommits.forEach(sonCommit -> updateToBeAddedCommits(sonCommit, toBeAddedCommits, deDuplication));
            // 将toBeAddedCommits按照时间排序
            toBeAddedCommits.sort(Comparator.comparing(RevCommit::getCommitTime));
        }
        scannedCommit.forEach(commit -> allCommits.removeIf(r -> r.equals(commit)));
        return allCommits;
    }

    private void updateToBeAddedCommits(RevCommit revCommit, List<RevCommit> toBeAddedCommits, Set<RevCommit> deDuplication) {
        RevCommit[] parents = revCommit.getParents();
        boolean canBeAdded = true;
        for (RevCommit parent : parents) {
            if (!deDuplication.contains(parent)) {
                canBeAdded = false;
                break;
            }
        }
        if (canBeAdded && !deDuplication.contains(revCommit)) {
            toBeAddedCommits.add(revCommit);
            deDuplication.add(revCommit);
        }
    }


    /**
     * 由小到大排序
     * st.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).forEach(e -> result.put(e.getKey(), e.getValue()));
     * 默认由大到小排序
     * 类型 V 必须实现 Comparable 接口，并且这个接口的类型是 V 或 V 的任一父类。这样声明后，V 的实例之间，V 的实例和它的父类的实例之间，可以相互比较大小。
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();
        st.sorted(Map.Entry.comparingByValue()).forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    /**
     * 获取指定父节点和当前commit的 diff 信息
     *
     * @param parentCommit 父版本
     * @param currCommit   当前版本
     * @param score        匹配度
     * @return {@link List<DiffEntry> 修改信息}
     */
    @SneakyThrows
    public List<DiffEntry> getDiffEntry(RevCommit parentCommit, RevCommit currCommit, int score) {
        // 不可少 否则parentCommit的 tree为null
        parentCommit = revWalk.parseCommit(ObjectId.fromString(parentCommit.getName()));
        TreeWalk tw = new TreeWalk(repository);
        tw.addTree(parentCommit.getTree());
        tw.addTree(currCommit.getTree());
        tw.setRecursive(true);
        RenameDetector rd = new RenameDetector(repository);
        rd.addAll(DiffEntry.scan(tw));
        rd.setRenameScore(score);
        return rd.compute();
    }




    @SneakyThrows
    private List<DiffEntry> getDiffCaseMerge(RevCommit parent1, RevCommit parent2, RevCommit curCommit) {
        List<DiffEntry> curToParent1 = getDiffEntry(parent1, curCommit, 60);
        List<DiffEntry> curToParent2 = getDiffEntry(parent2, curCommit, 60);
        List<DiffEntry> result = new ArrayList<>();
        if (isParent2(parent1, parent2, curCommit)) {
            List<DiffEntry> temp = curToParent1;
            curToParent1 = curToParent2;
            curToParent2 = temp;
        }

        // oldPath 相同
        for (DiffEntry diffEntry1 : curToParent1) {
            for (DiffEntry diffEntry2 : curToParent2) {
                // fixme 暂未考虑重命名的情况 或者无需考虑重命名的情况
                //  如 p1 a=a1  p2 a=>a2 是否冲突待验证
                boolean isSame = diffEntry1.getOldPath().equals(diffEntry2.getOldPath()) &&
                        diffEntry1.getNewPath().equals(diffEntry2.getNewPath());

                if (isSame) {
                    result.add(diffEntry1);
                }
            }
        }
        return result;
    }

    /**
     * 判断 parent2 是否为当前节点的直接父节点
     *
     * @param parent1    当前节点的父节点1
     * @param parent2    当前节点的父节点2
     * @param currCommit 当前节点
     * @return true： parent2 是直接父节点， false : parent1是直接父节点
     */
    public boolean isParent2(RevCommit parent1, RevCommit parent2, RevCommit currCommit) {
        String author1 = parent1.getAuthorIdent().getName();
        String author2 = parent2.getAuthorIdent().getName();
        String author = currCommit.getAuthorIdent().getName();
        if (author.equals(author2) && !author.equals(author1)) {
            return true;
        }
        if (!author.equals(author2) && author.equals(author1)) {
            return false;
        }
        return parent2.getCommitTime() > parent1.getCommitTime();
    }


    private DiffFormatter format(OutputStream os) {
        DiffFormatter df = new DiffFormatter(os);
        //如果加上这句，就是在比较的时候不计算空格，WS的意思是White Space
        df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        df.setRepository(repository);
        return df;
    }




}

