package org.sganslandt.watcher.core.health;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Collection;

public interface ServiceDAO {
    @SqlUpdate("create table if not exists Service (serviceName varChar(255) primary key)")
    void createServicesTable();

    @SqlUpdate("create table if not exists URL (serviceName varChar(255), url varchar(4096), primary key (serviceName, url))")
    void createURLsTable();

    @SqlUpdate("insert into Service (serviceName) values (:name)")
    void addService(@Bind("name") String serviceName);

    @SqlUpdate("delete from Service where serviceName = :name")
    void removeService(@Bind("name") String serviceName);

    @SqlUpdate("insert into URL (serviceName, url) values (:name, :url)")
    void addNode(@Bind("name") String serviceName, @Bind("url") String url);

    @SqlUpdate("delete from URL where serviceName = :name and url = :url")
    void removeNode(@Bind("name") String serviceName, @Bind("url") String url);

    @SqlUpdate("delete from URL where serviceName = :name")
    void removeAllNodes(@Bind("name") String serviceName);

    @SqlQuery("select serviceName from Service")
    Collection<String> listServices();

    @SqlQuery("select url from URL where serviceName = :serviceName")
    Collection<String> listNodes(@Bind("serviceName") String serviceName);
}
