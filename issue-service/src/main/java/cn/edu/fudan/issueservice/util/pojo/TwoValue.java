package cn.edu.fudan.issueservice.util.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * description: 工具类 pojo  用来存储只有两个值的对象
 *
 * @author fancying
 * create: 10/5/2022
 **/
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TwoValue<T1, T2> {
    T1 first;
    T2 second;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TwoValue<?, ?> twoValue = (TwoValue<?, ?>) o;
        return Objects.equals(first, twoValue.first) && Objects.equals(second, twoValue.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
