const fs = require("fs");
const path = require("path");
const prettier = require("prettier");

const root = path.resolve(__dirname, "..", "src", "main", "webapp");
const checkOnly = process.argv.includes("--check");

function findJspFiles(directory) {
    return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
        const target = path.join(directory, entry.name);
        if (entry.isDirectory()) {
            return findJspFiles(target);
        }
        return entry.isFile() && entry.name.endsWith(".jsp") ? [target] : [];
    });
}

function restoreJspTags(source) {
    return source.replaceAll("__colon__", ":");
}

async function main() {
    const files = findJspFiles(root);
    const unformatted = [];

    for (const file of files) {
        const source = fs.readFileSync(file, "utf8");
        const config = (await prettier.resolveConfig(file)) || {};
        const formatted = restoreJspTags(prettier.format(source, { ...config, filepath: file }));

        if (source !== formatted) {
            if (checkOnly) {
                unformatted.push(path.relative(process.cwd(), file));
            } else {
                fs.writeFileSync(file, formatted, "utf8");
                console.log(path.relative(process.cwd(), file));
            }
        }
    }

    if (checkOnly && unformatted.length > 0) {
        console.error("Prettierの整形が必要なJSP:");
        unformatted.forEach((file) => console.error(`- ${file}`));
        process.exitCode = 1;
        return;
    }

    console.log(checkOnly ? `全${files.length}件のJSPは整形済みです。` : `全${files.length}件のJSPを整形しました。`);
}

main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
});
