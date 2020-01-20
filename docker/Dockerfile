FROM azul/zulu-openjdk-centos:11

ENV JAVA_HOME /usr/lib/jvm/zulu11
RUN \
    set -xeu && \
    yum -y -q install less && \
    yum -q clean all && \
    rm -rf /var/cache/yum && \
    mkdir -p /usr/lib/yanagishima

ARG YANAGISHIMA_VERSION
COPY yanagishima-${YANAGISHIMA_VERSION} /usr/lib/yanagishima

EXPOSE 8080
ENV LANG en_US.UTF-8
WORKDIR /usr/lib/yanagishima
CMD ["./bin/yanagishima-run.sh"]
