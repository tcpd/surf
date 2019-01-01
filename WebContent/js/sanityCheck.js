function sanityCheck()
{
    var file = document.myform.myfile.value;
    console.log(file);
    var type = file.substring(file.lastIndexOf(".")+1, file.length).toLowerCase();
    if (type!="csv")
    {
        alert("Please enter a csv file!");
        console.log("Please enter a csv file!");
        return false;
    }
    return true;
}

function sanityCheck2()
{
    var file = document.myform.myfile.value;
    console.log(file);
    var type = file.substring(file.lastIndexOf(".")+1, file.length).toLowerCase();
    if (type!="csv")
    {
        alert("Please enter a csv file!");
        console.log("Please enter a csv file!");
        return false;
    }
    else
    {
        alert("File successfully uploaded!");
        console.log("File successfully uploaded!");
    }
    return true;
}