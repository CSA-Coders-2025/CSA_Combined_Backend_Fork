async function addOrder(trainId,startId,endId,repeat) {
    let dataOut;
    let body = {
        repeat: repeat,
        startId: startId,
        endId: endId
    }
    await fetch("/api/train/add/train/"+trainId.toString()+"/order/move", {
        method: "POST",
        body: JSON.stringify(body),
        cache: "no-cache",
        headers: new Headers({
            "content-type": "application/json"
        })
    }).then((response) => response.json()).then((data) => {
        dataOut = data;
    })
    return dataOut;
}

document.getElementById("moveOrderButton").addEventListener("click",async()=>{
    let train = document.getElementById("trainId").value;
    let start = document.getElementById("startId").value;
    let end = document.getElementById("endId").value;
    let repeat = document.getElementById("repeat").checked;
    let response = await addOrder(train,start,end,repeat);
    console.log(response);
})