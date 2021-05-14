# Deployment

# Production
Highly recommend to deploy in HTTPS due to security, clipboard copy, desktop notification

# Authentication and authorization
yanagishima doesn't have authentication/authorization feature.

But, if you have any reverse proxy server for yanagishima and that reverse proxy server provides HTTP level authentication, you can use it for yanagishima too.
yanagishima can log username for each query executions and authorize per datasource.

If your reverse proxy server sets username on HTTP header just after authentication, before proxied requests you can use it.

In this case, please specify ```audit.http.header.name``` which is http header name to be passed through your proxy.

If you want to deny to access without username, please specify ```user.require=true```

If you set ```check.datasource=true``` and datasource list which you want to allow on HTTP header ```X-yanagishima-datasources``` through your proxy, authorization feature is enabled.

For example, if there are three datasources(aaa and bbb and ccc) and ```X-yanagishima-datasources=aaa,bbb``` is set, user can't access to datasource ccc.

If you use a Trino with LDAP, you need to specify ```auth.xxx=true``` in your application.yml
```yaml
server.port: 8080
presto.datasources: your-presto
presto.coordinator.server.your-presto: http://presto.coordinator:8080
catalog.your-presto: hive
schema.your-presto: default
sql.query.engines: presto
auth.your-presto: true
```

