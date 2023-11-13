<%@ page import="net.ymate.platform.commons.lang.BlurObject" %>
<%@ page import="net.ymate.platform.webmvc.util.WebUtils" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" trimDirectiveWhitespaces="true" pageEncoding="UTF-8" session="false" %>

<!DOCTYPE html>
<html lang="zh" class="md">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width">
    <base href="<%=WebUtils.baseUrl(request)%>"/>
    <title>
        <%
            int ret = BlurObject.bind(request.getAttribute("ret")).toIntValue();
            out.write(ret != 0 ? WebUtils.getOwner().getOwner().getI18n().load("messages", "error.page.title_wrong", "Wrong!") : WebUtils.getOwner().getOwner().getI18n().load("messages", "error.page.title_warn", "Tips!"));
        %>
    </title>
    <link rel="stylesheet" href="assets/error/error.css">
</head>
<%
    out.write("<body><div class=\"content\"><div class=\"icon");
    out.write(ret == 0 ? " icon-warning" : " icon-wrong");
    out.write("\"></div><h1>");
    Integer status = BlurObject.bind(request.getParameter("status")).toInteger();
    if (status != null) {
        out.write(WebUtils.httpStatusI18n(WebUtils.getOwner(), status));
    } else {
        out.write(StringUtils.trimToEmpty(BlurObject.bind(request.getAttribute("msg")).toStringValue()));
        if (ret != 0) {
            out.write(StringUtils.SPACE);
            out.write(WebUtils.getOwner().getOwner().getI18n().load("messages", "error.page.label_code", "Code:"));
            out.write(StringUtils.SPACE);
            out.write(String.valueOf(ret));
        }
    }
    out.write("</h1>");
    //
    String subtitle = BlurObject.bind(request.getAttribute("subtitle")).toStringValue();
    if (StringUtils.isNotBlank(subtitle)) {
        out.write("<p id=\"subtitle\">");
        out.write("<span>" + subtitle + "</span>");
        //
        String moreUrl = BlurObject.bind(request.getAttribute("moreUrl")).toStringValue();
        if (StringUtils.isNotBlank(moreUrl)) {
            out.write("<a class=\"learn-more-button\" href=\"" + moreUrl + "\">" + WebUtils.getOwner().getOwner().getI18n().load("messages", "error.page.btn_more_details", "For more details.") + "</a>");
        }
        out.write("</p>");
    }
    Object data = request.getAttribute("data");
    if (data instanceof Map) {
        if (!((Map) data).isEmpty()) {
            String labelName = WebUtils.getOwner().getOwner().getI18n().load("messages", "error.page.label_details", "Details are as follows:");
            out.write("<div><div class=\"detail\"><em>" + labelName + "</em><ul>");
            for (Object item : ((Map) data).values()) {
                out.write("<li>" + BlurObject.bind(item).toStringValue() + "</li>");
            }
            out.write("</ul></div><div class=\"clearer\"></div></div>");
        }
    }
    out.write("</div></body></html>");
%>