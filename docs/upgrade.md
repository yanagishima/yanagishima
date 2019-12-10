# Upgrade

If you want to upgrade yanagishima from `xxx` to `yyy`, steps are as follows
```
cd yanagishima-xxx
bin/yanagishima-shutdown.sh
cd ..
unzip yanagishima-yyy.zip
cd yanagishima-yyy
mv result result_bak
mv yanagishima-xxx/result .
cp yanagishima-xxx/data/yanagishima.db data/
cp yanagishima-xxx/conf/yanagishima.properties conf/
bin/yanagishima-start.sh
```
If it is necessary to migrate yanagishima.db or result file, you need to migrate.
