### 0721 增加控制扫描线程数的配置项
scanThreadNum=3
### 0726 增加是否启用debug模式 true:是 false:否
debugMode=false
### 0728 未新增 修改baseRepoPath意义为debug模式下的repo路径
### 0802 用于删除sonar扫描时的日志
SonarqubeLogHome=/home/fdse/codeWisdom/service/issue/log/sonar/
### 0919 用于指定线程池大小
parallelScanRepoSize=3
repoQueueCapacity=30
### 1008 用于存放临时文件的，用于做文件的diff
diff.file.dir.prefix=/home/fdse/codeWisdom/service/issue/solve-way/temp/
