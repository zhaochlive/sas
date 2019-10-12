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
// 紧商网用户名username自动填充
$('.usernameJS').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/username",
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
            url: "search/SaleMan",
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
$('.buyCompany ').typeahead({
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
//买家公司名称填充
$('.InvoiceHeadUp ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getInvoiceHeadUp",
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
//仓库
$('.store ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getStore",
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


//商品名称
$('.productName ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getProductName",
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
//品牌
$('.brand ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getBrand",
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
//印记
$('.mark ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getMark",
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
//材质
$('.material ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getMaterial",
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
//牌号
$('.grade ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getGrade",
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
//表面处理
$('.surface ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getSurface",
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
//公称直径
$('.nominalDiameter ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getNominalDiameter",
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
//牙距
$('.pitch ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getPitch",
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
//长度
$('.extent ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getExtent",
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
//外径
$('.outerDiameter ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getOuterDiameter",
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
//厚度
$('.thickness ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getThickness",
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
//一级分类
$('.classOne ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getLevelOne",
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
//二级分类
$('.classTwo ').typeahead({
    source: function (query, process) {
        $.ajax({
            type: 'POST',
            url: "search/getLevelTwo",
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

