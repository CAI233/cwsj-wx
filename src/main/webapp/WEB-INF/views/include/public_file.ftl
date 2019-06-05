<script type="text/javascript">
    var ctxPath = '${ctxPath}';
    var MEMBERINFO
    <#if Session['member_json'] ??>
    MEMBERINFO=JSON.parse(('${Session["member_json"]}').replace(/[\n]/g,''))
    </#if>
    console.log(MEMBERINFO)
</script>
<!-- Required meta tags-->
<meta charset="utf-8">
<meta name="viewport"
      content="width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no, minimal-ui, viewport-fit=cover">
<meta name="apple-mobile-web-app-capable" content="yes">
<!-- Color theme for statusbar -->
<meta name="theme-color" content="#2196f3">
<link href="${ctxPath}/static/favicon.ico" rel="shortcut icon" type="image/x-icon"/>

<link rel="stylesheet" href="/static/mobile/plugin/swiper/swiper-4.2.0.min.css">
<link rel="stylesheet" href="/static/mobile/plugin/sui/css/sm.min.css">
<script type='text/javascript' src='/static/mobile/plugin/swiper/swiper-4.2.0.min.js' charset='utf-8'></script>
<script type='text/javascript' src='/static/mobile/plugin/sui/js/zepto.min.js' charset='utf-8'></script>
<script type='text/javascript' src='/static/mobile/plugin/sui/js/sm.min.js' charset='utf-8'></script>

<link rel="stylesheet" href="/static/mobile/plugin/sui/css/sm-extend.min.css">
<script type='text/javascript' src='/static/mobile/plugin/sui/js/sm-extend.min.js' charset='utf-8'></script>

<link rel="stylesheet" href="/static/mobile/font/iconfont.css">
<link rel="stylesheet" href="/static/mobile/css/animate.min.css">
<link rel="stylesheet" href="/static/mobile/css/public.css">
<script type='text/javascript' src='/static/mobile/js/public.js' charset='utf-8'></script>
