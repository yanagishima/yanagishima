FROM openjdk:11.0.5-jdk

MAINTAINER sunnywalden@gmailcom

#国内debian源
ADD sources.list /etc/apt/

RUN mkdir -p /root/.pip/

ADD pip.conf /root/.pip/

COPY . /tmp/yanagishima

ENV VERSION 21.0
ENV YANAGISHIMA_HOME /opt/yanagishima
ENV YANAGISHIMA_OPTS -Xmx3G
ENV TMP_PATH /tmp/yanagishima

# install node
RUN apt-get update && \
    apt-get install -y build-essential wget xz-utils && \
    cd /tmp && wget https://nodejs.org/dist/v12.14.0/node-v12.14.0-linux-x64.tar.xz && \
    xz -d node-v12.14.0-linux-x64.tar.xz && \
    tar -xf node-v12.14.0-linux-x64.tar && \
    mv node-v12.14.0-linux-x64 /usr/local/node && \
    rm -rf node-v12.14.0-linux-x64.tar.xz && \
    mkdir /root/.npm-global && \
    apt-get install -y python

ENV PATH /usr/local/node/bin:/root/.npm-global/bin:$PATH
ENV NPM_CONFIG_PREFIX /root/.npm-global

# deply yanagishima
RUN npm config set prefix '/root/.npm-global' && \
    npm config set registry https://registry.npm.taobao.org && \
    cd $TMP_PATH && \
    cd web && \
    npm install node-sass

RUN cd $TMP_PATH && \
    ./gradlew distZip && \
    cd build/distributions && \
    unzip yanagishima-$VERSION.zip && \
    rm -rf yanagishima-$VERSION.zip && \
    mv yanagishima-$VERSION /opt/ && \
    cd /opt && mv yanagishima-$VERSION yanagishima && \
    cd $YANAGISHIMA_HOME && \
    sed -i 's/"$@" &/"$@"/g' bin/yanagishima-start.sh && \
    rm -rf /tmp/yanagishima

WORKDIR /opt/yanagishima

CMD bin/yanagishima-start.sh


