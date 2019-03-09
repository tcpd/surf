/**
 * Created by sudx on 18/11/16.
 */
//SCRIPTS TO HANDLE COMMENTS

//function to strip alphabet from id
function stripId(commentId){
    var str = "";
    for(var i=0;i<commentId.length;i++){
        if(commentId[i]>='0' && commentId[i]<='9')
            str+=commentId[i];
    }
    return str;
};

//script function to handle comments
function commentHandler(commentId){
    var commentNode = document.getElementById(commentId);
    var id = stripId(commentId);
    var child = commentNode.childNodes[0];
    var text = commentNode.childNodes[1].innerText;
    commentNode.removeChild(commentNode.childNodes[1]);
    //The following snippet doesn't work.
    //if(child == text){
    //	child.focus();
    //	return;
    //}

    if(text==null){
        commentNode.appendChild(child);
    }
    else{
        var inputNode = document.createElement("textarea");
        inputNode.setAttribute("name", "commentParam"+id);
        inputNode.setAttribute("id","input"+commentId);
        inputNode.setAttribute("value", text);
        inputNode.setAttribute("textContent", text);
        inputNode.setAttribute("class", "form-control");
        inputNode.setAttribute("rows", "3");
        inputNode.setAttribute("cols", "20");
        inputNode.setAttribute("wrap", "hard");
        inputNode.setAttribute("onclick","");
        inputNode.innerText = text;
    }


    commentNode.replaceChild(inputNode, commentNode.childNodes[0]);

    var node = commentNode.childNodes[0];
    node.focus();
};

//name attribute gets created only when the checkbox is clicked; this is for efficiency; these are sent to servlet
function createNameParameter(id){
    var node=document.getElementById("isDone-"+id);
    node.setAttribute("name", "isDone-"+id);
    pnode = node.parentNode;
    if(($(pnode).children().length)<=1){
        hiddenNode = document.createElement("input")
        hiddenNode.setAttribute("type", "hidden")
        hiddenNode.setAttribute("name", node.id)
        hiddenNode.setAttribute("value", "off")
        pnode.appendChild(hiddenNode)
    }
}

//script to display the full comment

// function commentDisplayer(commentId){
//     var commentNode = document.getElementById(commentId);
//     var text = commentNode.innerText;
//     if(text.length>1){
//     	var commentBox = document.getElementById(commentId);
//     	var showMoreButton = document.createElement("button");
//     	showMoreButton.innerText ="Hello";
//     	showMoreButton.className += "btn btn-default"
//     	commentBox.appendChild(showMoreButton);
// 		var test = document.getElementById("test");
// 		test.style.color = "red";
//     }
// };



// //SETS VALUES FOR DROPDOWNS IN MODALS ON THE BASIS OF PREVIOUS SUBMISSION

// function SelectElement(id, valueToSelect){
// 	var element = document.getElementById(id);
// 	element.value = valueToSelect;
// }


cookieName="page_scroll"
expdays=365

window.onload = function () {
    loadScroll()
}

window.onunload = function () {
    saveScroll()
}

// An adaptation of Dorcht's cookie functions.

function setCookie(name, value, expires, path, domain, secure){
    if (!expires){expires = new Date()}
    document.cookie = name + "=" + escape(value) +
        ((expires == null) ? "" : "; expires=" + expires.toGMTString()) +
        ((path == null) ? "" : "; path=" + path) +
        ((domain == null) ? "" : "; domain=" + domain) +
        ((secure == null) ? "" : "; secure")
}

function getCookie(name) {
    var arg = name + "="
    var alen = arg.length
    var clen = document.cookie.length
    var i = 0
    while (i < clen) {
        var j = i + alen
        if (document.cookie.substring(i, j) == arg){
            return getCookieVal(j)
        }
        i = document.cookie.indexOf(" ", i) + 1
        if (i == 0) break;
    }
    return null
}

function getCookieVal(offset){
    var endstr = document.cookie.indexOf (";", offset)
    if (endstr == -1)
        endstr = document.cookie.length
    return unescape(document.cookie.substring(offset, endstr))
}

function deleteCookie(name,path,domain){
    document.cookie = name + "=" +
        ((path == null) ? "" : "; path=" + path) +
        ((domain == null) ? "" : "; domain=" + domain) +
        "; expires=Thu, 01-Jan-00 00:00:01 GMT"
}

function saveScroll(){ // added function
    //alert("saving")
    var expdate = new Date ()
    expdate.setTime (expdate.getTime() + (expdays*24*60*60*1000)); // expiry date
    //var bodyElement = document.getElementById("table-body")
    //var x = (bodyElement.pageXOffset?bodyElement.pageXOffset:bodyElement.scrollLeft)
    //var y = (bodyElement.pageYOffset?bodyElement.pageYOffset:bodyElement.scrollTop)
    var y = (parseInt(window.pageYOffset) || document.documentElement.scrollTop || document.body.scrollTop || 0)
    var x = (parseInt(window.pageXOffset) || document.documentElement.scrollLeft || document.body.scrollLeft || 0)
    //Data=x + "_" + y
    //setCookie(cookieName,Data,expdate)
    setCookie("window.pageYOffset", window.pageYOffset, expdate)
    setCookie("window.pageXOffset", window.pageXOffset, expdate)

    setCookie("document.documentElement.scrollTop", document.documentElement.scrollTop, expdate);
    setCookie("document.documentElement.scrollLeft", document.documentElement.scrollLeft, expdate);

    setCookie("document.body.scrollTop", document.body.scrollTop, expdate);
    setCookie("document.body.scrollLeft", document.body.scrollLeft, expdate);

}

function loadScroll(){ // added function
    //inf=getCookie(cookieName)
    //var bodyElement = document.getElementById("table-body")
    //var bodyElement = document.body
    if(getCookie("window.pageYOffset")!=null)
        window.pageYOffset = getCookie("window.pageYOffset").toString()
    if(getCookie("window.pageXOffset")!=null)
        window.pageXOffset = getCookie("window.pageXOffset").toString()
    if(getCookie("document.documentElement.scrollTop")!=null)
        document.documentElement.scrollTop = getCookie("document.documentElement.scrollTop")
    if(getCookie("document.documentElement.scrollLeft")!=null)
        document.documentElement.scrollLeft = getCookie("document.documentElement.scrollLeft")
    if(getCookie("document.body.scrollTop")!=null)
        document.body.scrollTop = getCookie("document.body.scrollTop")
    if(getCookie("document.body.scrollLeft")!=null)
        document.body.scrollLeft = getCookie("document.body.scrollLeft")

}

function resetScroll(){
    var bodyElement = document.getElementById("table-body")
    bodyElement.scrollLeft = 0
    bodyElement.scrollTop = 0

    window.pageYOffset = 0
    document.documentElement.scrollTop = 0
    document.body.scrollTop = 0
    bodyElement.scrollTop = 0

    window.pageXOffset = 0
    document.documentElement.scrollLeft = 0
    document.body.scrollLeft = 0
    bodyElement.scrollLeft = 0
}

function saveFilterSettings(){
    var expdate = new Date ()
    expdate.setTime (expdate.getTime() + (expdays*24*60*60*1000)); // expiry date
    setCookie("algorithm",document.getElementById("algorithm").value,expdate);
    //setCookie("dataset",document.getElementById("dataset").value,expdate);
    setCookie("onlyWinners",document.getElementById("onlyWinners").value,expdate);
}

function loadFilterSettings(){
    /*if(getCookie("algorithm")!="")
        document.getElementById("algorithm").value = getCookie("algorithm");
    if(getCookie("dataset")!="")
        document.getElementById("dataset").value = getCookie("dataset");
    if(getCookie("onlyWinners")!="")
        document.getElementById("onlyWinners").value = getCookie("onlyWinners");*/
    //CHECK table.jsp FOR filterVariables value.

    /*document.getElementById("algorithm").value = filterVariables[0]
    document.getElementById("dataset").value = filterVariables[1]
    document.getElementById("onlyWinners").value = filterVariables[2]
    document.getElementById("algo-arg").value = filterVariables[3]*/

    $(function () {
        $("#algorithm").val(filterVariables[0]);
        //$("#dataset").val(filterVariables[1]);
        $("#onlyWinners").val(filterVariables[2]);
        $("#algo-arg").val(filterVariables[3]);
        $("#comparatorType").val(filterVariables[4]);
        $("#filterParam").val(filterVariables[5]);
    });
}

//THESE FUNCTIONS USE HARD CODE TAGNAMES & VALUES;CHANGES MIGHT BE REQUIRED HERE IF THE TABLE CHANGES
function selectAllRowsInGroupForMerge(groupID) {
    rowList = document.getElementsByName(groupID)
    for(var i=0; i<rowList.length; i++){
        var row = rowList[i]
        if(row.childNodes.length>1 && row.childNodes[1].childNodes.length>0){
            var checkBoxElement = row.childNodes[1].childNodes[0]
            if(checkBoxElement.tagName == "INPUT" && checkBoxElement.type == "checkbox"){
                if(!checkBoxElement.checked)
                    checkBoxElement.click()
            }
        }
    }
}

function selectAllRowsInGroupForDone(groupID){
    rowList = document.getElementsByName(groupID)
    for(var i=0; i<rowList.length; i++){
        var row = rowList[i]
        if(row.childNodes.length>23 && row.childNodes[23].childNodes.length>1){
            var selectElement = row.childNodes[23].childNodes[1]
            if(selectElement.tagName == "INPUT" && selectElement.type == "checkbox"){
                if(selectElement.checked)   //TO MAke sure all are checked
                    $(selectElement).click()
                else{}
                $(selectElement).click()
            }
        }
    }
}

function selectUpTillHereForDone(groupID){
    nGroupID = parseInt(groupID.substring(1))
    userConsent = confirm("This will merge all rows in the " + (nGroupID-1) + " preceding groups. Proceed?")
    if(userConsent){
        for(var i=0; i<nGroupID; i++){
//            selectAllRowsInGroupForDone('g'+i)
            selectAllRowsInGroupForMerge('g'+i)
        }
    }
}

function resetButtonPressed(){
    userConsent = confirm("Warning: All rows in the dataset will be marked \"Not Done\". Are you sure");
    return userConsent;

}

//POPULATES DROPDOWN FOR FILTER VALUES

function populateDropdown() {
    var filterParamValue = document.getElementById("filterParam").value;
    while(filterValue.hasChildNodes()){
        filterValue.removeChild(filterValue.firstChild);
    }
    var allRecords = document.createElement("option");
    allRecords.textContent = "All Records";
    allRecords.value = "All Records";
    filterValue.appendChild(allRecords);
    values = filterDataValues[filterParamValue];
    values.sort();
    for(var i = 0; i < values.length; i++) {
        var opt = values[i];
        var el = document.createElement("option");
        el.textContent = opt;
        el.value = opt;
        filterValue.appendChild(el);
    };
}


//LOADING SCRIPT
$(window).on("load",function(){
    $('#loading').fadeOut();
});
//Script for Search button
$(window).on("load", function () {
    $('form').on('keyup keypress', function(e) {
        var keyCode = e.keyCode || e.which;
        if (keyCode === 13) {
            e.preventDefault();
        }
    });
    $("#searchValue").bind("enterKey",function (e) {
        $("#searchButton").click();
    });
    $("#searchValue").keyup(function(e){
        if(e.keyCode == 13){
            $(this).trigger("enterKey")
        }
    });

    $("#searchButton").on("click", function () {
        if($("#searchValue").val()=="")
            $("#algorithm").val("exactSameName");
        else {
            $("#algorithm").val("search");
            $("#algo-arg").val($("#searchValue").val());
        }
        $("#settingsSubmit").click();
        $("#loading").fadeIn();
        resetScroll();
    });
});

//Adjust table height
//on load
$(window).on("load", function () {
    $("#table-container").css("margin-top", $("nav.navbar").height()+10)
});
//on resize
$(window).resize(function () {
    $("#table-container").css("margin-top", $("nav.navbar").height()+10)
});

//Show warning on force merge
$(window).on("load", function () {
    $("#forceMergeButton").on("click", function () {
        userConsent = confirm("You are about to merge rows from multiple groups together. Are you sure?");
        if(!userConsent){
            return false
        }
        else{
            $('#loading').fadeIn();
            return true
        }
    });
});

//Mark all person for as done/reviewed
$(document).ready(function () {
    $("tr td.table-cell-done").click(function (event) {
        var temp = $(this).parent()[0];
        //console.log(temp)
        if($(this.firstElementChild).prop('checked')){
            $(this).parent().addClass('row-selected-for-done');
            while(temp.nextElementSibling!=null){
                if(temp.nextElementSibling.getAttribute("data-personid")==temp.getAttribute("data-personid")){
                    $(temp.nextElementSibling).addClass('row-selected-for-done')
                    temp = temp.nextElementSibling
                }else
                    break;
            }
        }else{
            $(this).parent().removeClass('row-selected-for-done')
            while(temp.nextElementSibling!=null){
                if(temp.nextElementSibling.getAttribute("data-personid")==temp.getAttribute("data-personid")){
                    $(temp.nextElementSibling).removeClass('row-selected-for-done')
                    temp = temp.nextElementSibling
                }else
                    break;
            }
        }
    })
});

//anchors shouldn't trigger parent's click events and mark the rows for selection
$(document).ready(function () {
    $('td a').click(function (e) {
        e.stopPropagation();
    })
});