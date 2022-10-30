# Violation
- 该项目通过静态分析工具对代码中存在的violations进行数据收集和分析，包括一下几个步骤:
1. 通过已有的静态分析工具对代码某单个版本代码进行扫描分析，得到该版本violations数据。
2. 对之后的版本代码逐个分析并与前一个（merge节点情况则可能是多个）版本进行缺陷匹配和追溯，分析每个violations的变化情况并记录。
3. 所有版本分析之后得到每一个violations的整个生命周期。

## 依赖配置
- 项目使用sonarqube扫描java项目 sonarqube 9.7.0下载并配置，
    - ![](https://obsidian-keyon.oss-cn-beijing.aliyuncs.com/pictures/202210302310073.png)
    - 配置文件中将sonarqube的前端端口，log目录配置
    - binHome中存储程序中调用sonar-scanner运行脚本executeSonarqube.sh
    - 配置sonar账号密码

- 项目使用TscanCode扫描cpp项目 版本2.14.24
    - ![](https://obsidian-keyon.oss-cn-beijing.aliyuncs.com/pictures/202210310059306.png)
    - 配置tscanCode的log目录
    - 在executeTscanCode.sh中进行cpp项目分析。
- mysql数据库配置
    - src/main/resources/database.sql 文件中执行sql创建mysql表进行存储
    - violations的信息和版本间追溯的信息。
    - 把issue_type.sql中语句执行，把可识别的violations类型插入数据库
    - 其中issue表中记录了所有的violations所有信息包括当前状态
    - raw_issue表中记录了每个violation的不同部分的信息
    - raw_issue_match_info表中记录了violations的追溯信息，标识每个版本的状态，比如引入版本（add），变动版本（change），消除版本（solved）等
- mongodb配置
    - 使用mongodb进行静态工具分析中间结果存储
    - 下载mongodb后
    - 运行一下语句
 ```  
   use issueTracker;
   db.createCollection("raw_issue_cache") 
   ```
## 其他配置
- ![](https://obsidian-keyon.oss-cn-beijing.aliyuncs.com/pictures/202210310120882.png)
## 项目运行
- 运行springboot项目之后请求serverIp:8080/issue/scan
- 可传参数body
- repoUuid(项目的标识，取repoPrefix的后面一个字段，即项目最后级的目录),
- branch（分支）,
- startCommit（第一个要扫描的commit）,
- endCommit（最后一个要扫描的commit，不指明的话到最近的commit）
- 扫描之后再接着上次的结果扫描只用传入repoUuid和branch

## 数据结果分析
- 通过数据库中查询repoUuid标识的该项目下的violations信息
- 在数据库部分已经介绍表的含义


