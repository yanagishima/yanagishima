FROM openjdk:11.0.5-jdk

#国内debian源
ADD sources.list /etc/apt/

RUN mkdir -p /root/.pip/

ADD pip.conf /root/.pip/

COPY . /tmp/yanagishima

WORKDIR /opt/yanagishima

# install node
RUN apt-get update && \
    apt-get install -y build-essential wget xz-utils && \
    wget https://nodejs.org/dist/v12.14.0/node-v12.14.0-linux-x64.tar.xz && \
    xz -d node-v12.14.0-linux-x64.tar.xz && \
    tar -xf node-v12.14.0-linux-x64.tar && \
    mv node-v12.14.0-linux-x64 /usr/local/node && \
    rm -rf /node-v12.14.0-linux-x64.tar.xz && \
    mkdir /root/.npm-global && \
    # install python
    apt-get install -y python

ENV PATH /usr/local/node/bin:/root/.npm-global/bin:$PATH
ENV NPM_CONFIG_PREFIX /root/.npm-global

# deply yanagishima
RUN npm config set prefix '/root/.npm-global' && \
    npm config set registry https://registry.npm.taobao.org && \
    cd /tmp/yanagishima && \
#    npm install --unsafe-perm -g node-sass && \
    cd web && \
    npm install node-sass && \
    ./gradlew distZip && \
    cd build/distributions && \
    unzip yanagishima-21.0 && \
    mv yanagishima-21.0 /opt/yanagishima

ENTRYPOINT ['/bin/bash', '-c', 'bin/yanagishima-start.sh']


