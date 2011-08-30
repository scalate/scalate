<%
  // TODO lots of Java code here to make the people collection :)
%>

<h1>Table Example</h1>

<div class="main">
  <table>
    <tr>
      <th>Name</th>
      <th>Location</th>
    </tr>
    <form:forEachMapEntry items="${people}" var="person">
    	<tr>
    		<td>${person.key}</td>
    		<td>${person.value}</td>
    	</tr>
    	<tr>
    </form:forEachMapEntry>
  </table> 

<div class="description">
  <p>
    This is lots and lots of text.
    <b>Scalate</b> really is rather awesome!
  </p>      
</div>