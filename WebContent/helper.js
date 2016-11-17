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

//name attribute gets created only when the dropdown is clicked; this is for efficiency
function createNameParameter(id){
    var node=document.getElementById("isDone-"+id);
    node.setAttribute("name", "isDone-"+id);
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
    var expdate = new Date ()
    expdate.setTime (expdate.getTime() + (expdays*24*60*60*1000)); // expiry date
    var bodyElement = document.getElementById("table-body")
    //var x = (bodyElement.pageXOffset?bodyElement.pageXOffset:bodyElement.scrollLeft)
    //var y = (bodyElement.pageYOffset?bodyElement.pageYOffset:bodyElement.scrollTop)
    var x = bodyElement.scrollLeft
    var y = bodyElement.scrollTop
    Data=x + "_" + y
    setCookie(cookieName,Data,expdate)
}

function loadScroll(){ // added function
    inf=getCookie(cookieName)
    var bodyElement = document.getElementById("table-body")
    if(!inf){return}
    var ar = inf.split("_")
    if(ar.length == 2){
        //bodyElement.scrollTo(parseInt(ar[0]), parseInt(ar[1]))
        bodyElement.scrollLeft = ar[0]
        bodyElement.scrollTop = ar[1]
    } else {
        resetScroll()
    }
}

function resetScroll(){
    var bodyElement = document.getElementById("table-body")
    bodyElement.scrollLeft = 0
    bodyElement.scrollTop = 0
}

function saveFilterSettings(){
    var expdate = new Date ()
    expdate.setTime (expdate.getTime() + (expdays*24*60*60*1000)); // expiry date
    setCookie("algorithm",document.getElementById("algorithm").value,expdate);
    setCookie("dataset",document.getElementById("dataset").value,expdate);
    setCookie("onlyWinners",document.getElementById("onlyWinners").value,expdate);
}

function loadFilterSettings(){
    if(getCookie("algorithm")!="")
        document.getElementById("algorithm").value = getCookie("algorithm");
    if(getCookie("dataset")!="")
        document.getElementById("dataset").value = getCookie("dataset");
    if(getCookie("onlyWinners")!="")
        document.getElementById("onlyWinners").value = getCookie("onlyWinners");
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
            if(selectElement.tagName == "SELECT"){
                selectElement.click()
                selectElement.value = "yes"
            }
        }
    }
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


//TESTING SCRIPT
//$("#test").on("click", function(){
//<%-- //	document.write("<%=request.getParameterValues("demerges")%>"); --%>
//});


//SCRIPT FOR HIGHLIGHTING AND CHECKING ROWS

$("document").ready(function(){
	$("tr td:not(:nth-last-child(2)):not(:last-child)").on("click", function(e){
		if($(e.target).closest('input[type="checkbox"]').length > 0){
			$(this).parent().toggleClass("success");
        }
		else{
			$(this).parent().toggleClass("success");
			var checkboxValue = $(this).parent().find("td:first-child input[type]").prop("checked");
			$(this).parent().find("td:first-child input[type]").prop("checked", !checkboxValue);
		}

	});
});



//LOADING SCRIPT
$(window).on("load",function(){
    $('#loading').fadeOut();
});

