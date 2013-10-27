package org.n3r.biz.pagestatic.spec;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.joor.Reflect;

import java.util.List;

public class Specs {
    public static <T> List<T> newObjects(String specExpr, Class<T> clazz) {
        List<T> paramValidators = Lists.newArrayList();
        if (StringUtils.isEmpty(specExpr)) return paramValidators;

        for (Spec spec : SpecParser.parseSpecs(specExpr)) {
            T paramValidator = parseTag(clazz, spec);

            paramValidators.add(paramValidator);
        }

        return paramValidators;
    }

    public static <T> T parseTag(Class<T> clazz, Spec spec) {
        T object = Reflect.on(clazz).create().get();

        if (object instanceof SpecAppliable)
            ((SpecAppliable) object).applySpec(spec);

        return object;
    }

}
