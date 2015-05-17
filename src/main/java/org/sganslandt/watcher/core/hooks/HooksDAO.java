package org.sganslandt.watcher.core.hooks;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Collection;
import java.util.List;

public interface HooksDAO {
    @SqlUpdate("create table if not exists Hook (url varChar(255) primary key)")
    void createTable();

    @SqlUpdate("insert into Hook (url) values (:url)")
    void addHook(@Bind("url") String url);

    @SqlUpdate("delete from Hook where url = :url")
    void removeHook(@Bind("url") String url);

    @SqlQuery("select url from Hook")
    Collection<String> listHooks();

}
