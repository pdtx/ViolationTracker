#!/bin/bash

repoPath=${1}
repoUuid=${2}
version=${3}

/home/dependency/TscanCodeV2.14.24.linux/TscanCodeV2.14.2395.linux/tscancode -j 4 --xml ${repoPath} 2>/home/fdse/user/codeWisdom/service/issue/log/tscan    code/err-${repoUuid}_${version}.xml 1>/home/fdse/user/codeWisdom/service/issue/log/tscancode/info-${repoUuid}_${version}.txt