package com.mnlin.linemenuview.dagger.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * 功能----fragment对应dagger的生命周期控制
 * <p>
 * Created by MNLIN on 2017/9/23.
 */
@Scope
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface PerFragment {

}
