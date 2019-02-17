function sanityCheck()
{
    var file = document.myform.myfile.value;
    var type = file.substring(file.lastIndexOf(".")+1, file.length).toLowerCase();
    if (type!="csv")
    {
        alert("Please enter a csv file!");
        return false;
    }

    if (document.myform.desc.value === '') {
        alert ("Please provide a description");
        return false;
    }
    return true;
}

function sanityCheck2()
{
    var file = document.myform.myfile.value;
    var type = file.substring(file.lastIndexOf(".")+1, file.length).toLowerCase();
    if (type!="csv")
    {
        alert("Please enter a csv file!");
        return false;
    }
    else
    {
        alert("Good, that file works!");
    }
    return true;
}