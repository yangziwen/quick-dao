package io.github.yangziwen.quickdao.example.repository.helper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;

import io.github.yangziwen.quickdao.example.entity.User;
import io.github.yangziwen.quickdao.example.enums.Gender;
import io.github.yangziwen.quickdao.example.repository.UserElasticSearchRepository;

public class UserElasticSearchHelper {

    public static int prepareData(UserElasticSearchRepository repository) {
        List<User> list = new ArrayList<>();

        list.add(User.builder()
                .username("张一")
                .gender(Gender.FEMALE)
                .city("北京")
                .age(25)
                .createTime(parseDateQuietly("2021-01-20"))
                .build());

        list.add(User.builder()
                .username("张二")
                .gender(Gender.MALE)
                .city("上海")
                .age(30)
                .createTime(parseDateQuietly("2021-01-21"))
                .build());

        list.add(User.builder()
                .username("张三")
                .gender(Gender.MALE)
                .city("南京")
                .age(26)
                .createTime(parseDateQuietly("2021-02-05"))
                .build());

        list.add(User.builder()
                .username("张四")
                .gender(Gender.FEMALE)
                .city("上海")
                .age(20)
                .createTime(parseDateQuietly("2021-02-01"))
                .build());

        list.add(User.builder()
                .username("张五")
                .gender(Gender.MALE)
                .city("北京")
                .age(34)
                .createTime(parseDateQuietly("2021-02-02"))
                .build());

        list.add(User.builder()
                .username("李一")
                .gender(Gender.MALE)
                .city("北京")
                .age(29)
                .createTime(parseDateQuietly("2021-02-05"))
                .build());

        list.add(User.builder()
                .username("李二")
                .gender(Gender.MALE)
                .city("北京")
                .age(23)
                .createTime(parseDateQuietly("2021-01-18"))
                .build());

        list.add(User.builder()
                .username("李三")
                .gender(Gender.FEMALE)
                .city("上海")
                .age(23)
                .createTime(parseDateQuietly("2021-01-17"))
                .build());

        list.add(User.builder()
                .username("李四")
                .gender(Gender.MALE)
                .city("南京")
                .age(28)
                .createTime(parseDateQuietly("2021-01-13"))
                .build());

        list.add(User.builder()
                .username("李五")
                .gender(Gender.MALE)
                .city("上海")
                .age(32)
                .createTime(parseDateQuietly("2021-01-02"))
                .build());

        list.add(User.builder()
                .username("王一")
                .gender(Gender.MALE)
                .city("北京")
                .age(27)
                .createTime(parseDateQuietly("2021-01-05"))
                .build());

        list.add(User.builder()
                .username("王二")
                .gender(Gender.MALE)
                .city("北京")
                .age(25)
                .createTime(parseDateQuietly("2021-01-03"))
                .build());

        list.add(User.builder()
                .username("王三")
                .gender(Gender.FEMALE)
                .city("上海")
                .age(28)
                .createTime(parseDateQuietly("2021-01-05"))
                .build());

        list.add(User.builder()
                .username("王四")
                .gender(Gender.FEMALE)
                .city("天津")
                .age(25)
                .createTime(parseDateQuietly("2021-01-27"))
                .build());

        list.add(User.builder()
                .username("王五")
                .gender(Gender.MALE)
                .city("上海")
                .age(25)
                .createTime(parseDateQuietly("2021-02-13"))
                .build());

        list.add(User.builder()
                .username("赵一")
                .gender(Gender.FEMALE)
                .city("南京")
                .age(27)
                .createTime(parseDateQuietly("2021-02-17"))
                .build());

        list.add(User.builder()
                .username("赵二")
                .gender(Gender.MALE)
                .city("上海")
                .age(31)
                .createTime(parseDateQuietly("2021-02-11"))
                .build());

        list.add(User.builder()
                .username("赵三")
                .gender(Gender.MALE)
                .city("北京")
                .age(30)
                .createTime(parseDateQuietly("2021-02-14"))
                .build());

        list.add(User.builder()
                .username("赵四")
                .gender(Gender.MALE)
                .city("沈阳")
                .age(37)
                .createTime(parseDateQuietly("2021-02-11"))
                .build());

        list.add(User.builder()
                .username("赵五")
                .gender(Gender.MALE)
                .city("上海")
                .age(37)
                .createTime(parseDateQuietly("2021-02-14"))
                .build());

        return repository.batchInsert(list, 10);
    }

    public static int clearData(UserElasticSearchRepository repository) {
        List<Object> ids = repository.list().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        return repository.deleteByIds(ids);
    }

    private static Date parseDateQuietly(String date) {
        try {
            return DateUtils.parseDate(date, "yyyy-MM-dd");
        } catch (ParseException e) {
            return null;
        }
    }

}
