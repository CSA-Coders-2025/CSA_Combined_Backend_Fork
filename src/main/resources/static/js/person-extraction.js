import BlobBuilder from "./extraction.js";

async function getPeople(){
    await fetch("/mvc/extract/all/person", {
                method: "GET",
                cache: "no-cache",
        }).then((response) => response.json()).then((data) => {
           return data;
    })
}

document.getElementById("export-all").addEventListener("click",async ()=>{
    let content = await getPeople();
    const blobBuilder = new BlobBuilder(BlobBuilder.fileTypeEnum.json,content);
})