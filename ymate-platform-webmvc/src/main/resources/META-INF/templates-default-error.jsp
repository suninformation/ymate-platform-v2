<%@ page import="net.ymate.platform.commons.lang.BlurObject" %>
<%@ page import="net.ymate.platform.webmvc.util.WebUtils" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" trimDirectiveWhitespaces="true" pageEncoding="UTF-8" session="false" %>

<html lang="zh" class="md">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width">
    <title>
        <%
            int ret = BlurObject.bind(request.getAttribute("ret")).toIntValue();
            out.write(ret != 0 ? WebUtils.getOwner().getOwner().getI18n().load("messages", "error.page.title_wrong", "Wrong!") : WebUtils.getOwner().getOwner().getI18n().load("messages", "error.page.title_warn", "Tips!"));
        %>
    </title>
    <style type="text/css">
        .icon-warning {
            background-image: url(data:image/png;base64,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);
            background-size: contain;
        }

        .icon-wrong {
            background-image: url(data:image/png;base64,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);
            background-size: contain;
        }

        body {
            -webkit-font-smoothing: antialiased;
            margin: 0;
            background-color: #303030;
        }

        .content {
            color: rgba(255, 255, 255, 0.7);
            font-size: 14px;
            line-height: 16px;
            min-width: 240px;
        }

        h1 {
            color: rgba(255, 255, 255, 0.8);
            font-size: 20px;
            font-weight: 400;
            line-height: 22px;
        }

        em {
            color: white;
            font-style: normal;
        }

        .learn-more-button {
            color: rgb(123, 170, 247);
            text-decoration: none;
        }

        html {
            direction: ltr;
        }

        body {
            font-family: system-ui, PingFang SC, STHeiti, sans-serif;
        }

        @media (max-width: 240px),
        (max-height: 320px) {
            .content {
                font-size: 14px;
                line-height: 16px;
            }

            h1 {
                font-size: 14px;
                line-height: 16px;
            }
        }

        .icon {
            height: 120px;
            width: 120px;
        }

        @media (max-height: 480px),
        (max-width: 720px) {
            .icon {
                height: 72px;
                width: 72px;
            }
        }

        @media (max-width: 720px) {
            @media (max-width: 240px),
            (max-height: 480px) {
                .icon {
                    height: 48px;
                    width: 48px;
                }
            }
        }

        .content > .learn-more-button {
            display: none;
        }

        @media (max-width: 720px) {
            #subtitle > .learn-more-button {
                display: none;
            }

            .content > .learn-more-button {
                display: block;
            }
        }

        .content {
            margin-left: auto;
            margin-right: auto;
            max-width: 600px;
        }

        .icon {
            margin-left: auto;
            margin-right: auto;
        }

        h1 {
            text-align: center;
        }

        .detail {
            float: left;
        }

        html[dir=rtl] .detail {
            float: right;
        }

        .detail + .detail {
            clear: right;
        }

        html[dir=rtl] .detail + .detail {
            clear: left;
        }

        .clearer {
            clear: both;
        }

        @media (max-width: 720px) {
            .content {
                -webkit-margin-start: 0;
                max-width: 600px !important;
                text-align: start;
            }

            .icon {
                -webkit-margin-start: 0;
            }

            h1 {
                text-align: start;
            }

            .detail + .detail,
            html[dir=rtl] .detail + .detail {
                clear: both;
            }
        }

        .detail ul {
            -webkit-padding-start: 16px;
            -moz-padding-start: 16px;
            margin: 4px 0 0;
        }

        ul > li {
            padding-top: 8px;
        }

        .detail {
            margin: 0;
        }

        .detail + .detail {
            -webkit-margin-start: 40px;
        }

        .detail + .detail.too-wide {
            -webkit-margin-start: 0;
            margin-top: 1.5rem;
        }

        @media (min-width: 720px) {
            .icon, h1, #subtitle, .learn-more-button {
                margin-bottom: 1.5rem;
                margin-top: 1.5rem;
            }

            .content {
                margin-top: 40px;
                min-width: 240px;
                padding: 8px 48px 24px;
            }

            @media (max-height: 480px) {
                html,
                body,
                .content {
                    height: 100%;
                }

                .content {
                    margin-bottom: 0;
                    margin-top: 0;
                    padding-bottom: 0;
                    padding-top: 0;
                }

                .icon {
                    margin-top: 0;
                    padding-top: 32px;
                }
            }

            @media (max-height: 320px) {
                h1, #subtitle, .learn-more-button {
                    margin-bottom: 16px;
                    margin-top: 16px;
                }

                .icon {
                    margin-bottom: 16px;
                }
            }
        }

        @media (max-width: 720px) {
            .content {
                padding: 72px 32px;
                min-width: 176px;
            }

            .icon, h1, #subtitle, .learn-more-button {
                margin-bottom: 1.5rem;
                margin-top: 1.5rem;
            }

            .detail + .detail {
                -webkit-margin-start: 0;
                margin-top: 1.5rem;
            }

            @media (max-height: 600px) {
                .content {
                    padding-top: 48px;
                }

                .icon, h1, #subtitle, .learn-more-button {
                    margin-bottom: 1rem;
                    margin-top: 1rem;
                }

                .detail + .detail {
                    margin-top: 1rem;
                }
            }

            @media (max-height: 480px) {
                .content {
                    padding-top: 32px;
                }
            }

            .icon {
                margin-top: 0;
            }

            .learn-more-button {
                margin-bottom: 0;
            }
        }

        @media (max-width: 240px) {
            .content {
                min-width: 192px;
                padding-left: 24px;
                padding-right: 24px;
            }
        }
    </style>
</head>
<%
    out.write("<body><div class=\"content\"><div class=\"icon");
    out.write(ret == 0 ? " icon-warning" : " icon-wrong");
    out.write("\"></div><h1>");
    Integer status = BlurObject.bind(request.getParameter("status")).toInteger();
    if (status != null) {
        out.write(WebUtils.httpStatusI18n(WebUtils.getOwner(), status));
    } else {
        out.write(BlurObject.bind(request.getAttribute("msg")).toStringValue());
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