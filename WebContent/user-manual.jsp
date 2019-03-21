<%@ page contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"
         import="java.util.*"
%>
<%@ page import="in.edu.ashoka.surf.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <link rel="icon" type="image/png" href="images/surf-favicon.png">
	<link href="https://fonts.googleapis.com/css?family=Sacramento" rel="stylesheet">
    <link href="css/fonts/font-awesome/css/font-awesome-4.7.min.css" rel="stylesheet">

	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<!--	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous"> -->

    <link rel="stylesheet" href="css/bootstrap.min.css">
    <!-- Optional theme -->
    <link rel="stylesheet" href="css/bootstrap-theme.min.css">

    <link rel="stylesheet" type="text/css" href="css/main.css">
	<link rel="stylesheet" type="text/css" href="css/surf.css">

    <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.12.1/jquery.min.js"></script>
    <script type="text/javascript"> if (!window.jQuery) {document.write('<script type="text/javascript" src="js/jquery-1.12.1.min.js"><\/script>');}</script>

    <script type="text/javascript" src="//maxcdn.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js"></script>
    <script type="text/javascript"> if (!(typeof $().modal == 'function')) { document.write('<script type="text/javascript" src="js/bootstrap-3.1.1.min.js"><\/script>'); }</script>


	<title>Surf</title>
    <style>
     h3 { font-weight: 600 ; font-size: 16pt; padding: 20px 0px; }
        .data { font-style: italic}
    </style>
</head>
<body style="margin:5%; width:90%">

<a class="logo" style="font-size:30px;margin-left:20px;" href="#">Surf</a>
<br/>
<br/>
<br/>

<h1>Surf user manual</h1>
<div class="help">Work in progress</div>
<br/>

<h3>About Surf</h3>
<p>
Welcome to Surf, a tool for cleaning messing data. Surf has one simple purpose: to assign IDs to rows based on the values in a given column in a dataset.
This is useful, for example, when a dataset has a column of person names, and you need to assign a unique ID to each individual. It is most effective
    for datasets with a few hundred rows to a few hundred thousand rows. Surf was originally developed at the <a href="http://tcpd.ashoka.edu.in">Trivedi Center for Political Data</a>
    at <a href="http://ashoka.edu.in">Ashoka University</a>,
for the purpose of tracking career trajectories of candidates in Indian elections. However, it is a standalone tool that can be used for other datasets as well.

<p>
Surf does not assign IDs automatically, but it throws up rows that have possibly similar values in the column. A human analyst makes all judgements
about deciding whether 2 rows should have the same ID.

<h3>Clustering in Surf</h3>
<p>
Surf's fundamental operation is to cluster rows that may have the same ID by grouping together rows that have "similar" values in a specified column. For this purpose, Surf has a range of clustering options for
names that have small mispellings, different formatting or ordering (<span class="data">Lal Krishna Advani; Lalkrushna Advani; ADVANI, LAL KISHAN</span>),
    to names that are "compatible" using initialization (<span class="data">L.K. Advani; Lal Krishna Advani </span>)
    to names that are contained in another (<span class="data">PRAFUL PATEL; PRAFUL CHANDRA PATEL</span>)
    to names that have a certain number of words in common (<span class="data">P V GAJAPATHI RAJU; GAJAPATHI RAJU GARU</span>).

<p>
Before clustering rows, Surf performs the following steps:
<br/>
1.    It normalizes upper and lower case and removes honorifics and titles like Mr, Mrs, Dr. General, etc.

<br/>
2. It normalizes the name to take care of common variations in spellings.
    This normalization internally rewrites names based on a set of rules that are particularly tuned for Indian people or place names
    (but may work for other names as well). For example, V is frequently substituted for W, X for KSH, etc.
    The list of normalization rules is configurable in the Surf source code, although it currently cannot be controlled from the user interface.
    (contact us if you need help.)

<br/>
3. It retokenizes the name by analyzing patterns in the dataset and breaking up words that may have been combined. For example, in a dataset of Indian
politicians, Surf can detect that the word KUMAR is quite common, and therefore break up the single word VIJAYKUMAR into two words, VIJAY KUMAR.
The broken-up words are used for internal analysis by some clustering algorithms.
    <br/>
    4. It sorts the words in the name. Therefore <span class="data">First-name Last-name</span> will be the
    same as <span>Last-name First-name </span> after normalization.


<h3>Why not OpenRefine, Excel or other data cleaning tools?</h3>

Tools like OpenRefine allow you to create clusters of rows and harmonize the values in the column of interest. However, since they do not let
you assign IDs to the rows, they are not useful when the main goal is to disambiguate different identities. Let's say you used Open Refine to harmonize
names with slightly different spellings; there is still the possibility that two names are exactly the same, but need to have a different ID. You cannot
capture this information since OpenRefine does not track IDs. All other data cleaning tools we are aware of have this limitation, and that is why
we developed Surf. However, Surf does use some of the same edit distance clustering code that is in OpenRefine.
<p>
This kind of data cleaning for ID assignment is slow, painful, and incomplete with spreadsheets like MS Excel.
Real-life datasets need a lot of data preparation (normalization, tokenization etc.), along with sophisticated algorithms for
clustering. (We used to assign IDs by sorting on the person name, and hoping names for the same person would
be visible close by, but realized how unworkable that was. It is also extremely difficult to track IDs manually.)
While we still use spreadsheets to sometimes eyeball data and perform spot checks, we have completely switched to Surf for ID assignment.

<h3>What is the workflow with Surf?</h3>

To work with a dataset, you upload it into Surf, run different clustering algorithms, perform merges, and then download the
dataset back when you are done. The downloaded dataset will have all the rows and columns as the original one; the only difference is that
there is now an ID column.
<p>

You can run Surf in multiple sessions; if IDs have already been assigned, they will be used, and rows with the same ID will be shown together.
    The clustering algorithms and the display of rows will treat all rows with the same ID field as equivalent.

<p>
    Typically, we recommend merges using the following flow with Surf. However, you may need to adapt it depending on the importance of finding all merges, the size of the dataset,
    and the time available.<p>

    1. Run the edit distance clustering algorithm (first with max. edit distance set to 0, then 1, and then 2 if you wish.)<br/>
    Increasing settings of the maximum edit distances suggest more merges, but can also cause more noise.  <br/>
    2. Run the compatible names clustering algorithm (good default settings are minimum token overlap of 2 and ignore token frequency of about 2-3% of the number of rows in your dataset; however this can vary widely depending on your requirements.)<br/>
    3. Run the streaks algorithm if the rows that you are merging are likely to have contiguous streaks of values in some other column. This is useful particularly for longitudinal datasets,
    where if an ID is present in rows that have values 4, 5, 6 and 8 in a secondary column, there is also likely to be a row with the same ID and the value 7 in the secondary column.
    In this case, Surf will look harder for a match amongst rows with value 7 in the secondary column.<br/>
    For example, in the Indian elections dataset, we know that if a particular person has been running in Assembly numbers 4, 5, 6 and 8, they are also likely to have run in Assembly 7.
    4. If you know of merges in the dataset that Surf is not suggesting in any of its algorithms, you can simply use the "All IDs in a single cluster" algorithm that shows all names in one giant cluster.
    You can then use filter to narrow down to the rows of interest and merge them.<br/>
    5. Finally, you can review all the merges performed using the "One cluster per ID" algorithm. This lets you look for any incorrect merges.


</p>
<h3>How is Surf distributed?</h3>

Surf is distributed as a web-hosted or standalone browser-based application (packaged in a DMG file for Mac, EXE file for windows, or JAR file for any Java-based system).
For windows distributions, the Java runtime environment (version 8 or higher) is also required to be pre-installed.

Surf operates with a familiar, browser-based frontend. (Chrome is best, although other browsers should also work. In the standalone mode, Surf starts a local webserver which means
everything happens in the privacy of your own machine and no data is sent out of your machine. Your browser connects to this standalone browser.

Surf's packaging is similar to other open-source data management tools like <a href="http://openrefine.org">OpenRefine</a> and <a href="https://tabula.technology/">Tabula</a>.

<h3>FAQ</h3>

A FAQ will be available soon. Send suggested questions for the FAQ to hangal@ashoka.edu.in.

<h3>Support</h3>

To report problems with Surf, or to suggest enhancements, please write to hangal@ashoka.edu.in and we will do our best to help.

</body>
</html>
