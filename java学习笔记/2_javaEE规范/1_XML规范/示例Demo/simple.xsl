<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/">
<html>
<body>
	<xsl:element name="h2">my love</xsl:element>
	<xsl:for-each select="books/book">
		<p><xsl:value-of select="name"/><a href="#{generate-id(name)}">查看详情</a></p>
	</xsl:for-each>
	<table border="1">
		<tr bgcolor="#55A0FF">
			<th>名称</th> 
			<th>出版社</th>
			<th>公司</th>
			<th>作者</th>
			<th>ISBN</th>
			<th>价格</th>
			<th>网站URL</th>
		</tr>
	
	<xsl:for-each select="books/book">
	<xsl:sort select="price"/>
	<xsl:if test="author='张小侠'">
		<tr bgcolor="#7D7DFF">
			<td><xsl:value-of select="name"/></td>
			<td><xsl:value-of select="publisher"/></td>
			<td><xsl:value-of select="company"/></td>
			<td><xsl:value-of select="author"/></td> 
			<td><xsl:value-of select="ISBN"/></td>
			<td><xsl:value-of select='format-number(price,"###,###.00")'/></td>
			<td><xsl:value-of select="url"/></td>
		</tr>
	</xsl:if>
	</xsl:for-each>
	</table>
</body>
</html>
</xsl:template>
</xsl:stylesheet>