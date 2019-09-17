//地区自动填充
$('.province').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/area",
            data: {
                name: query,
                limit: "8"
            },
            dataType: "json",
            success: function (result) {
                return process(result.data);
            }, error: function (error) {
                console.log(error);
            }
        });
    }
});
//业务员搜索条件
$('.salesman').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/waysales",
            data: {
                name: query,
                limit: "8"
            },
            dataType: "json",
            success: function (result) {
                return process(result.data);
            }, error: function (error) {
                console.log(error);
            }
        });
    }
});
//客户名称提示填充
$('.username').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/CustomerMan",
            data: {
                name: query,
                limit: "8"
            },
            dataType: "json",
            success: function (result) {
                return process(result.data);
            }, error: function (error) {
                console.log(error);
            }
        });
    }
});
//商家公司名称自动填充
$('.SellerCompany').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "/search/getSellerCompanyName",
            data: {
                name: query,
                limit: "8"
            },
            dataType: "json",
            success: function (result) {
                return process(result.data);
            }, error: function (error) {
                console.log(error);
            }
        });
    }
});
//商家、店铺名称自动填充
$('.shopname').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "/search/shopName",
            data: {
                name: query,
                limit: "8"
            },
            dataType: "json",
            success: function (result) {
                return process(result.data);
            }, error: function (error) {
                console.log(error);
            }
        });
    }
});
//客服自动填充
$('.clerkname').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/clerkMan",
            data: {
                name: query,
                limit: "8"
            },
            dataType: "json",
            success: function (result) {
                return process(result.data);
            }, error: function (error) {
                console.log(error);
            }
        });
    }
});
//分类/标准自动填充
$('.classify').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/classify",
            data: {
                name: query,
                limit: "8"
            },
            dataType: "json",
            success: function (result) {
                return process(result.data);
            }, error: function (error) {
                console.log(error);
            }
        });
    }
});
//牌号自动填充
$('.gradeno').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getGradeNo",
            data: {
                name: query,
                limit: "8"
            },
            dataType: "json",
            success: function (result) {
                return process(result.data);
            }, error: function (error) {
                console.log(error);
            }
        });
    }
});
//规格自动填充
$('.standard').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getStandard",
            data: {
                name: query,
                limit: "8"
            },
            dataType: "json",
            success: function (result) {
                return process(result.data);
            }, error: function (error) {
                console.log(error);
            }
        });
    }
});
//买家公司名称填充
$('.buyCompany').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getBuyerCompanyname",
            data: {
                name: query,
                limit: "8"
            },
            dataType: "json",
            success: function (result) {
                return process(result.data);
            }, error: function (error) {
                console.log(error);
            }
        });
    }
});

