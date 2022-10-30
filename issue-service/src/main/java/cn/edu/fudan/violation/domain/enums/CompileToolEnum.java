package cn.edu.fudan.violation.domain.enums;

/**
 * description: 编译工具
 *
 * @author fancying
 * create: 2020-03-16 09:39
 **/
public enum CompileToolEnum {
    /**
     * 编译工具的名字
     *
     * @value 编译工具需要的文件名
     */
    MAVEN("pom.xml"),
    GRADLE("build.gradle");

    private final String compileFile;

    CompileToolEnum(String compileFile) {
        this.compileFile = compileFile;
    }

    public String compileFile() {
        return this.compileFile;
    }


}
