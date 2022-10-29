module.paths.push("/Users/beethoven/Desktop/saic/issue-tracker-web/node_modules")

const babelEslint = require("babel-eslint");
const options = {
    range: false,

    loc: true,

    comment: true,

    tokens: true,

    ecmaVersion: 11,

    sourceType: "module",

    ecmaFeatures: {

        jsx: true,

        globalReturn: true,

        impliedStrict: true
    }
}
process.argv.forEach(function (val, index) {
    if (index === 1) {
        const args = process.argv.slice(2);
        const fs = require('fs');
        const data = fs.readFileSync(args[0], {encoding: "utf8"});
        const ast = babelEslint.parse(`${data}`, options);
        console.log(JSON.stringify(ast));
    }
});
