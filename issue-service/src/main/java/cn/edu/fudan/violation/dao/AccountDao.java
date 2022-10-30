package cn.edu.fudan.violation.dao;

import cn.edu.fudan.violation.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Joshua
 * @description
 * @date 2022-03-04 19:46
 **/
@Repository
public class AccountDao {

    private AccountMapper accountMapper;

    public List<String> getGitnameByAccountName(String majorAccountName) {
        return accountMapper.getGitnameByAccountName(majorAccountName);
    }

    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }
}
