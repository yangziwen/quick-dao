package io.github.yangziwen.quickdao.example.repository;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.sql2o.Sql2o;

import io.github.yangziwen.quickdao.core.Criteria;
import io.github.yangziwen.quickdao.core.Query;
import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.sql2o.BaseSql2oRepository;

public class UserSql2oRepository extends BaseSql2oRepository<User> {

    public UserSql2oRepository(Sql2o sql2o) {
        super(sql2o);
    }

    public List<User> getUserListByUsernames(String...usernames) {
        if (ArrayUtils.isEmpty(usernames)) {
            return Collections.emptyList();
        }
        Criteria criteria = new Criteria().and("username").in(usernames);
        return list(new Query().where(criteria));
    }

}
