package cn.edu.fudan.issueservice.domain.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Beethoven
 */

public class IssueTypeInChineseEnums {

    private static Map<String, String> javaIssueTypeInChineseEnumMap;
    private static Map<String, String> jsIssueTypeInChineseEnumMap;
    private static Map<String, String> cppIssueTypeInChineseEnumMap;
    private static boolean init = false;

    public static String getRuleChineseName(String tool, String rule) {

        if (!init) {
            init();
        }

        if (ToolEnum.SONAR.getType().equals(tool)) {
            return javaIssueTypeInChineseEnumMap.getOrDefault(rule,rule);
        } else if (ToolEnum.ESLINT.getType().equals(tool)) {
            return jsIssueTypeInChineseEnumMap.getOrDefault(rule,rule);
        } else if (ToolEnum.TSCANCODE.getType().equals(tool)) {
            return cppIssueTypeInChineseEnumMap.getOrDefault(rule,rule);
        }

        return null;
    }

    private static void init() {
        javaIssueTypeInChineseEnumMap = JavaIssueTypeInChineseEnum.getMapToChinese();
        jsIssueTypeInChineseEnumMap = JsIssueTypeInChineseEnum.getMapToChinese();
        cppIssueTypeInChineseEnumMap = CppIssueTypeInChineseEnum.getMapToChinese();

        init = true;
    }

    public enum JavaIssueTypeInChineseEnum {
        /**
         * JAVA规则中英文对照
         */
        RULE_1("\".equals()\" should not be used to test the values of \"Atomic\" classes", "不要使用equals方法对Atomic类进行是否相等的判断"),
        RULE_2("\"@Deprecated\" code should not be used", "不应该在程序中使用 @Deprecated 标注的接口、类和方法"),
        RULE_3("\"BigDecimal(double)\" should not be used", "不要使用BigDecimal(double)去构造一个BigDecimal对象"),
        RULE_4("\"clone\" should not be overridden", "不应重写clone方法"),
        RULE_5("\"equals(Object obj)\" and \"hashCode()\" should be overridden in pairs", "equals(Object obj)和hashCode()应该同时重写"),
        RULE_6("\"Iterator.next()\" methods should throw \"NoSuchElementException\"", "Iterator.next()应该抛出NoSuchElementException异常"),
        RULE_7("\"notifyAll\" should be used", "此处应该使用notifyAll而不是notify"),
        RULE_8("\"null\" should not be used with \"Optional\"", "null不应与Optional一起使用"),
        RULE_9("\"NullPointerException\" should not be caught", "空指针不应捕获处理"),
        RULE_10("\"NullPointerException\" should not be explicitly thrown", "NullPointerException不应该被显式抛出"),
        RULE_11("\"Thread.run()\" should not be called directly", "不应该直接调用Thread对象的run方法"),
        RULE_12("\"wait\" should not be called when multiple locks are held", "当有多个锁时，不应调用“wait”"),
        RULE_13("[p3c]Do not add 'is' as prefix while defining Boolean variable.", "[p3c]定义布尔变量时不要添加“is”作为前缀"),
        RULE_14("[p3c]Do not remove or add elements to a collection in a foreach loop.", "[p3c]不要在foreach循环中删除或向集合添加元素"),
        RULE_15("[p3c]Do not use methods which will modify the list after using Arrays.asList to convert array to list.", "[p3c]不要使用在使用后会修改列表的方法数组Arrays.asList"),
        RULE_16("[p3c]Manually create thread pool is better.", "[p3c]最好手动创建线程池"),
        RULE_17("[p3c]SimpleDataFormat is unsafe, do not define it as a static variable. If have to, lock or DateUtils class must be used.", "[p3c]SimpleDataFormat不安全，不要将其定义为静态变量。如果必须定义，则必须使用lock或DateUtils类"),
        RULE_18("Boxing and unboxing should not be immediately reversed", "装箱（创建int/Integer类型值的对象）和拆箱（将对象中原始值解出来）不应连续操作"),
        RULE_19("Collection sizes and array length comparisons should make sense", "集合大小和数组长度比较应该是有意义的"),
        RULE_20("Conditionally executed blocks should be reachable", "条件执行块应该是可访问的"),
        RULE_21("Empty arrays and collections should be returned instead of null", "应该返回空数组和集合，而不是null"),
        RULE_22("Instance methods should not write to \"static\" fields", "实例方法不应写入静态字段"),
        RULE_23("Locks should be released", "保证锁能够释放"),
        RULE_24("Loops should not be infinite", "循环不应该是无限的"),
        RULE_25("Math operands should be cast before assignment", "赋值前应转换数学操作数"),
        RULE_26("Related \"if/else if\" statements should not have the same condition", "if语句不应该有相同的条件"),
        RULE_27("Resources should be closed", "资源应该关闭"),
        RULE_28("String literals should not be duplicated", "字符串文字不应重复"),
        RULE_29("Strings and Boxed types should be compared using \"equals()\"", "字符串和包装类型对比时应该使用equals方法"),
        RULE_30("Try-with-resources should be used", "应该使用Try-with-resources"),
        RULE_31("Week Year (\"YYYY\") should not be used for date formatting", "星期 年（“YYYY”）不应用于日期格式"),
        RULE_32("Zero should not be a possible denominator", "零不应该是一个分母"),
        //106和85规则名不同
        RULE_33("Conditionally executed code should be reachable", "条件执行块应该是可访问的");

        private final String name;
        private final String nameInChinese;

        JavaIssueTypeInChineseEnum(String name, String nameInChinese) {
            this.name = name;
            this.nameInChinese = nameInChinese;
        }

        public static Map<String, String> getMapToChinese() {
            Map<String, String> map = new HashMap<>(256);
            for (JavaIssueTypeInChineseEnum issueTypeInChineseEnum : JavaIssueTypeInChineseEnum.values()) {
                map.put(issueTypeInChineseEnum.name, issueTypeInChineseEnum.nameInChinese);
            }
            return map;
        }
    }

    public enum JsIssueTypeInChineseEnum {
        /**
         * JS 规则中英文对照
         */
        RULE_1("arrow-spacing", "强制箭头函数的箭头前后使用一致的空格"),
        RULE_2("block-spacing", "禁止或强制在代码块中开括号前和闭括号后有空格"),
        RULE_3("brace-style", "强制在代码块中使用一致的大括号风格"),
        RULE_4("camelcase", "强制使用骆驼拼写法命名约定"),
        RULE_5("comma-dangle", "要求或禁止末尾逗号"),
        RULE_6("comma-spacing", "强制在逗号前后使用一致的空格"),
        RULE_7("comma-style", "强制使用一致的逗号风格"),
        RULE_8("curly", "强制所有控制语句使用一致的括号风格"),
        RULE_9("dot-location", "强制在点号之前和之后一致的换行"),
        RULE_10("eqeqeq", "要求使用 === 和 !=="),
        RULE_11("generator-star-spacing", "强制 generator 函数中 * 号周围使用一致的空格"),
        RULE_12("indent", "强制使用一致的缩进"),
        RULE_13("key-spacing", "强制在对象字面量的属性中键和值之间使用一致的间距"),
        RULE_14("keyword-spacing", "强制在关键字前后使用一致的空格"),
        RULE_15("new-cap", "要求构造函数首字母大写"),
        RULE_16("new-parens", "强制或禁止调用无参构造函数时有圆括号"),
        RULE_17("no-array-constructor", "禁用 Array 构造函数"),
        RULE_18("no-caller", "禁用 arguments.caller 或 arguments.callee"),
        RULE_19("no-class-assign", "禁止修改类声明的变量"),
        RULE_20("no-cond-assign", "禁止条件表达式中出现赋值操作符"),
        RULE_21("no-const-assign", "禁止修改 const 声明的变量"),
        RULE_22("no-control-regex", "禁止在正则表达式中使用控制字符"),
        RULE_23("no-delete-var", "禁止删除变量"),
        RULE_24("no-dupe-args", "禁止 function 定义中出现重名参数"),
        RULE_25("no-dupe-class-members", "禁止类成员中出现重复的名称"),
        RULE_26("no-dupe-keys", "禁止对象字面量中出现重复的 key"),
        RULE_27("no-duplicate-case", "禁止出现重复的 case 标签"),
        RULE_28("no-empty-character-class", "禁止在正则表达式中使用空字符集"),
        RULE_29("no-empty-pattern", "禁止使用空解构模式"),
        RULE_30("no-eval", "禁用 eval()"),
        RULE_31("no-ex-assign", "禁止对 catch 子句的参数重新赋值"),
        RULE_32("no-extend-native", "禁止扩展原生类型"),
        RULE_33("no-extra-bind", "禁止不必要的 .bind() 调用"),
        RULE_34("no-extra-boolean-cast", "禁止不必要的布尔转换"),
        RULE_35("no-extra-parens", "禁止不必要的括号"),
        RULE_36("no-fallthrough", "禁止 case 语句落空"),
        RULE_37("no-floating-decimal", "禁止数字字面量中使用前导和末尾小数点"),
        RULE_38("no-func-assign", "禁止对 function 声明重新赋值"),
        RULE_39("no-implied-eval", "禁止使用类似 eval() 的方法"),
        RULE_40("no-inner-declarations", "禁止在嵌套的块中出现变量声明或 function 声明"),
        RULE_41("no-invalid-regexp", "禁止 RegExp 构造函数中存在无效的正则表达式字符串"),
        RULE_42("no-irregular-whitespace", "禁止不规则的空白"),
        RULE_43("no-iterator", "禁用 __iterator__ 属性"),
        RULE_44("no-lone-blocks", "禁用不必要的嵌套块"),
        RULE_45("no-mixed-spaces-and-tabs", "禁止空格和 tab 的混合缩进"),
        RULE_46("no-multi-spaces", "禁止使用多个空格"),
        RULE_47("no-multi-str", "禁止使用多行字符串"),
        RULE_48("no-multiple-empty-lines", "禁止出现多行空行"),
        RULE_49("no-native-reassign", "不允许修改只读全局变量"),
        RULE_50("no-negated-in-lhs", "不允许否定in表达式中的左操作数"),
        RULE_51("no-new-object", "禁用 Object 的构造函数"),
        RULE_52("no-new-require", "禁止调用 require 时使用 new 操作符"),
        RULE_53("no-new-symbol", "禁止 Symbolnew 操作符和 new 一起使用"),
        RULE_54("no-new-wrappers", "禁止对 String，Number 和 Boolean 使用 new 操作符"),
        RULE_55("no-obj-calls", "禁止把全局对象作为函数调用"),
        RULE_56("no-octal", "禁用八进制字面量"),
        RULE_57("no-octal-escape", "禁止在字符串中使用八进制转义序列"),
        RULE_58("no-proto", "禁用 __proto__ 属性"),
        RULE_59("no-redeclare", "禁止多次声明同一变量"),
        RULE_60("no-regex-spaces", "禁止正则表达式字面量中出现多个空格"),
        RULE_61("no-return-assign", "禁止在 return 语句中使用赋值语句"),
        RULE_62("no-self-assign", "禁止自我赋值"),
        RULE_63("no-self-compare", "禁止自身比较"),
        RULE_64("no-sequences", "禁用稀疏数组"),
        RULE_65("no-throw-literal", "禁止抛出异常字面量"),
        RULE_66("no-trailing-spaces", "禁用行尾空格"),
        RULE_67("no-undef", "禁用未声明的变量，除非它们在 /*global */ 注释中被提到"),
        RULE_68("no-undef-init", "禁止将变量初始化为 undefined"),
        RULE_69("no-unexpected-multiline", "禁止出现令人困惑的多行表达式"),
        RULE_70("no-unmodified-loop-condition", "禁用一成不变的循环条件"),
        RULE_71("no-unneeded-ternary", "禁止可以在有更简单的可替代的表达式时使用三元操作符"),
        RULE_72("no-unreachable", "禁止在 return、throw、continue 和 break 语句之后出现不可达代码"),
        RULE_73("no-unsafe-finally", "禁止在 finally 语句块中出现控制流语句"),
        RULE_74("no-unused-vars", "禁止出现未使用过的变量"),
        RULE_75("no-useless-call", "禁止不必要的 .call() 和 .apply()"),
        RULE_76("no-useless-computed-key", "禁止在对象中使用不必要的计算属性"),
        RULE_77("no-useless-constructor", "禁用不必要的构造函数"),
        RULE_78("no-useless-escape", "禁用不必要的转义字符"),
        RULE_79("no-whitespace-before-property", "禁止属性前有空白"),
        RULE_80("no-with", "禁用 with 语句"),
        RULE_81("one-var", "强制函数中的变量要么一起声明要么分开声明"),
        RULE_82("operator-linebreak", "强制操作符使用一致的换行符"),
        RULE_83("padded-blocks", "要求或禁止块内填充"),
        RULE_84("quotes", "强制使用一致的反勾号、双引号或单引号"),
        RULE_85("semi", "要求或禁止使用分号代替 ASI"),
        RULE_86("semi-spacing", "强制分号之前和之后使用一致的空格"),
        RULE_87("space-before-blocks", "强制在块之前使用一致的空格"),
        RULE_88("space-before-function-paren", "强制在 function的左括号之前使用一致的空格"),
        RULE_89("space-in-parens", "强制在圆括号内使用一致的空格"),
        RULE_90("space-infix-ops", "要求操作符周围有空格"),
        RULE_91("space-unary-ops", "强制在一元操作符前后使用一致的空格"),
        RULE_92("spaced-comment", "强制在注释中 // 或 /* 使用一致的空格"),
        RULE_93("template-curly-spacing", "要求或禁止模板字符串中的嵌入表达式周围空格的使用"),
        RULE_94("use-isnan", "要求使用 isNaN() 检查 NaN"),
        RULE_95("valid-typeof", "强制 typeof 表达式与有效的字符串进行比较"),
        RULE_96("wrap-iife", "要求 IIFE 使用括号括起来"),
        RULE_97("yield-star-spacing", "强制在 yield* 表达式中 * 周围使用空格"),
        RULE_98("yoda", "要求或禁止 “Yoda” 条件"),
        RULE_99("prefer-const", "要求使用 const 声明那些声明后不再被修改的变量"),
        RULE_100("no-debugger", "禁用 debugger"),
        RULE_101("object-curly-spacing", "强制在大括号中使用一致的空格"),
        RULE_102("array-bracket-spacing", "强制数组方括号中使用一致的空格");


        private final String name;
        private final String nameInChinese;

        JsIssueTypeInChineseEnum(String name, String nameInChinese) {
            this.name = name;
            this.nameInChinese = nameInChinese;
        }

        public static Map<String, String> getMapToChinese() {
            Map<String, String> map = new HashMap<>(256);
            for (JsIssueTypeInChineseEnum issueTypeInChineseEnum : JsIssueTypeInChineseEnum.values()) {
                map.put(issueTypeInChineseEnum.name, issueTypeInChineseEnum.nameInChinese);
            }
            return map;
        }
    }

    public enum CppIssueTypeInChineseEnum {
        /**
         * C++规则中英文对照
         */
        RULE_0("nullpointer-explicitNullDereference", "显式空指针解应用"),
        RULE_1("suspicious-tooManyNestedScopes", "函数内嵌套层次太多"),
        RULE_2("bufoverrun-funcRetLengthAsIndex", "read函数返回值作为数组下标"),
        RULE_3("memleak-leakReturnValNotUsed", "内存分配函数的返回结果未使用"),
        RULE_4("compute-UnsignedDivision", "有符号数和无符号数进行除法运算"),
        RULE_5("nullpointer-nullPointerArray", "数组元素空指针"),
        RULE_6("bufoverrun-arrayIndexCheckDefect", "数组下标校验缺陷"),
        RULE_7("nullpointer-funcDereferenceBeforeCheck", "函数的引用返回值被先解引用后判空"),
        RULE_8("nullpointer-possibleDereferenceAfterCheck", "可能存在先判空后解引用，但是解引用已不在判空范围内"),
        RULE_9("suspicious-autovar", "返回局部变量地址"),
        RULE_10("nullpointer-nullPointerClass", "类成员变量没有判断空指针"),
        RULE_11("nullpointer-directFuncPossibleRetNull", "函数返回值直接解引用"),
        RULE_12("nullpointer-arrayDereferenceBeforeCheck", "数组被先解引用后判空"),
        RULE_13("logic-SwitchNoDefault", "default分支缺失"),
        RULE_14("bufoverrun-bufferAccessOutOfBounds", "数据缓冲区越界"),
        RULE_15("uninit-possibleUninitvar", "可能的变量未初始化"),
        RULE_16("compute-incrementboolean", "bool变量自增"),
        RULE_17("compute-moduloAlwaysTrueFalse", "取模运算结果错误判断"),
        RULE_18("logic-incorrectLogicOperator", "错误的逻辑运算操作"),
        RULE_19("logic-stlIfFind", "低效使用std::find"),
        RULE_20("compute-NegativeBitwiseShift", "位操作数为负"),
        RULE_21("nullpointer-funcFuncRetNull", "函数返回值赋值另一函数的引用返回值， 该引用未判空情况下解引用"),
        RULE_22("suspicious-suspiciousfor", "可疑的for循环"),
        RULE_23("logic-UnintentionalOverflow", "可能的表达式溢出"),
        RULE_24("compute-PreciseComparison", "浮点数判等"),
        RULE_25("logic-oppositeInnerCondition", "相反或相同的if条件"),
        RULE_26("logic-SwitchNoBreakUP", "break缺失"),
        RULE_27("nullpointer-funcRetNull", "函数返回值未判空解引用"),
        RULE_28("compute-ZeroDivision", "除零错误"),
        RULE_29("suspicious-unConditionalBreakinLoop", "无条件break"),
        RULE_30("compute-Unsignedlessthanzero", "无符号数判断小于0"),
        RULE_31("logic-clarifyCondition", "运算符优先级错误"),
        RULE_32("logic-StlMissingComparison", "迭代器无结束判断"),
        RULE_33("suspicious-IfCondition", "if条件赋值"),
        RULE_34("compute-SizeofForArrayParameter", "数组参数使用sizeof"),
        RULE_35("logic-ComparisonOfBoolWithInt", "bool和int比较"),
        RULE_36("logic-duplicateIf", "if条件重复"),
        RULE_37("nullpointer-dereferenceAfterCheck", "先判空后解引用"),
        RULE_38("logic-bitwiseOnBoolean", "bool进行位运算"),
        RULE_39("memleak-leakNoVarFunctionCall", "传递参数过程中分配内存"),
        RULE_40("bufoverrun-arrayIndexThenCheck", "先访问数组后校验下标范围"),
        RULE_41("logic-selfAssignment", "变量自赋值"),
        RULE_42("logic-stlSortCheck", "自定义比较函数缺陷"),
        RULE_43("nullpointer-arrayDereferenceAfterCheck", "数组取值先判空再解引用"),
        RULE_44("uninit-uninitMemberVar", "构造函数中成员变量未初始化"),
        RULE_45("nullpointer-dereferenceIfNull", "指针为空解引用"),
        RULE_46("bufoverrun-stlOutOfBounds", "stl库函数越界"),
        RULE_47("uninit-uninitstring", "字符串初始化错误"),
        RULE_48("bufoverrun-negativeIndex", "负数下标"),
        RULE_49("memleak-deallocDealloc", "重复dealloc"),
        RULE_50("uninit-uninitvar", "变量未初始化"),
        RULE_51("logic-ComparisonOfBoolWithBool", "bool和bool比较运算"),
        RULE_52("memleak-mismatchAllocDealloc", "内存申请和释放不匹配"),
        RULE_53("suspicious-Danglingpointer", "野指针"),
        RULE_54("bufoverrun-OutOfBoundsStatistic", "基于统计推断数据下标可能越界"),
        RULE_55("logic-redundantCondition", "重复的条件表达式"),
        RULE_56("uninit-possibleUninitMemberInCtor", "构造函数中成员变量可能未初始化"),
        RULE_57("logic-CompareDefectInFor", "for循环比较运算类型错误"),
        RULE_58("uninit-possibleUninitPtr", "可能的指针变量未初始化"),
        RULE_59("nullpointer-invalidDereferenceIterator", "无效枚举器解引用"),
        RULE_60("compute-SizeofForNumericParameter", "数字常量使用sizeof"),
        RULE_61("suspicious-BoolFuncReturn", "未返回bool变量"),
        RULE_62("suspicious-suspiciousSemicolon", "可疑的分号使用"),
        RULE_63("logic-RecursiveFunc", "函数递归调用"),
        RULE_64("uninit-possibleUninitStruct", "可能的未初始化结构体变量"),
        RULE_65("logic-SignedUnsignedMixed", "有符号无符号变量之间赋值错误"),
        RULE_66("unsafefunc-unsafeFunctionUsage", "高危函数"),
        RULE_67("bufoverrun-pointerOutOfBounds", "指针访问越界"),
        RULE_68("suspicious-FuncReturn", "函数返回值未处理"),
        RULE_69("uninit-uninitPtr", "指针变量未初始化"),
        RULE_70("bufoverrun-formatbufoverrun", "sprintf_s中越界错误"),
        RULE_71("suspicious-strPlusChar", "字符串加字符变量"),
        RULE_72("logic-integerMultiplyOverflow", "整数乘法运算溢出风险"),
        RULE_73("suspicious-invalidIterator", "迭代器使用错误"),
        RULE_74("suspicious-RenameLocalVariable", "变量定义重名"),
        RULE_75("suspicious-SuspiciousPriority", "可疑优先级"),
        RULE_76("nullpointer-funcRetNullStatistic", "基于统计推断函数返回值未判空"),
        RULE_77("nullpointer-checkNullDefect", "判空条件缺陷"),
        RULE_78("compute-sizeofsizeof", "嵌套sizeof"),
        RULE_79("memleak-mismatchSize", "内存分配大小错误"),
        RULE_80("uninit-UninitStruct", "未初始化结构体变量"),
        RULE_81("logic-ComparisonWithBool", "bool变量比较运算"),
        RULE_82("logic-redundantAssignment", "多余的赋值操作"),
        RULE_83("nullpointer-arrayFuncRetNull", "函数返回值赋值数组， 该数组未判空情况下解引用"),
        RULE_84("nullpointer-nullPointerArg", "函数的参数没有判断空指针"),
        RULE_85("logic-InvalidVarArgs", "格式化字符串参数不匹配"),
        RULE_86("suspicious-NestedLoop", "嵌套循环"),
        RULE_87("logic-duplicateBranch", "分支内容重复"),
        RULE_88("memleak-deallocuse", "dealloc后解引用"),
        RULE_89("suspicious-assignmentInAssert", "断言中变量赋值"),
        RULE_90("bufoverrun-arrayIndexOutOfBounds", "数组下标越界"),
        RULE_91("nullpointer-arrayDereferenceIfNull", "数组为空解引用"),
        RULE_92("nullpointer-missingDerefOperator", "可能缺少*号操作符"),
        RULE_93("logic-STLFindError", "std::find函数返回值判定错误"),
        RULE_94("memleak-memleak", "内存泄漏"),
        RULE_95("compute-SizeofForPointerSize", "指针变量使用sizeof"),
        RULE_96("logic-DuplicateExpression", "判断条件重复"),
        RULE_97("logic-NoFirstCase", "case缺失"),
        RULE_98("suspicious-wrongvarinfor", "for循环变量错误"),
        RULE_99("logic-unsignedMinusError", "无符号整数减法运算溢出风险"),
        RULE_100("memleak-resourceLeak", "资源泄漏"),
        RULE_101("memleak-memleakOnRealloc", "realloc内存泄漏"),
        RULE_102("nullpointer-dereferenceBeforeCheck", "先解引用后判空"),
        RULE_103("uninit-uninitMemberInCtor", "构造函数使用未初始化成员变量"),
        RULE_104("memleak-virtualDestructor", "虚析构函数使用错误"),
        RULE_105("nullpointer-possibleNullDereferenced", "可能的空指针解引用"),
        RULE_106("logic-assignIf", "无效的if表达式"),
        RULE_107("nullpointer-funcDereferenceAfterCheck", "函数先判空再解引用"),
        RULE_108("suspicious-memsetZeroBytes", "memset参数使用错误"),
        RULE_109("suspicious-suspiciousArrayIndex", "数组下标使用错误"),
        RULE_110("nullpointer-funcDereferenceIfNull", "函数引用返回值为空解应用"),
        RULE_111("bufoverrun-IndexSelfIncrementError", "数组下标在使用前自增");
        private final String name;
        private final String nameInChinese;

        CppIssueTypeInChineseEnum(String name, String nameInChinese) {
            this.name = name;
            this.nameInChinese = nameInChinese;
        }

        public static Map<String, String> getMapToChinese() {
            Map<String, String> map = new HashMap<>(256);
            for (CppIssueTypeInChineseEnum issueTypeInChineseEnum : CppIssueTypeInChineseEnum.values()) {
                map.put(issueTypeInChineseEnum.name, issueTypeInChineseEnum.nameInChinese);
            }
            return map;
        }
    }
}
