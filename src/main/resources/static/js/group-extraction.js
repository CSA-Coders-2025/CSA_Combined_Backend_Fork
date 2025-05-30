import BlobBuilder from "./blob-builder.js";

async function getAllGroup() {
    let data1;
    await fetch("/api/groups/bulk/extract", {
        method: "GET",
        cache: "no-cache",
    }).then((response) => response.json()).then((data) => {
        data1 = data;
    })
    return data1;
}

document.getElementById("export-all-groups").addEventListener("click", async () => {
    let content = await getAllGroup();
    const blob = new BlobBuilder(BlobBuilder.fileTypeEnum.json, content);
    blob.downloadBlob("groups");
})

