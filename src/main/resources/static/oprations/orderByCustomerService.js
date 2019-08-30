function loadTable() {
    var keys = null;
    var colums = [];
    var data = [];

    colums.push({field: 'clerkname', title: '客服名称', sortable: false, align: 'center'},
        {field: 'username', title: '用户名称', sortable: false, align: 'center'},
        {
            field: 'companyname', title: '公司名称', sortable: false, align: 'center',
            formatter: function (value, row, index) { // 单元格格式化函数
                if (value) {
                    return "<div style='width:200px;'>" + row.companyname + "</div>";//调列宽，在td中嵌套一个div，调整div大小
                }else {
                    return "-";
                }
            }
        }
    );
    $.ajax({
        url: "customerService/getColums",
        type: "post",
        async: false,
        data: {
            startDate: startDate,
            endDate: endDate
        },
        dataType: "json",
        success: function (resultData) {
            keys = resultData;
            if (resultData) {
                $.each(resultData, function (index, value) {
                    colums.push({field: value, title: value, sortable: false, align: 'center'});
                });
            }
        },
    });

    initTable(colums, data);
}

function initTable(colums, data) {

    $("#dataTable table").bootstrapTable('destroy').bootstrapTable({
        url: "/customerService/getData",
        method: "get",
        cache: false,
        clickToSelect: true,
        pagination: true,
        sortable: true,
        // sortName:"name",
        // sortOrder: "desc",     //排序方式
        sortOrder: "DESC",
        height: 580,
        pageNumber: 1,
        pageSize: 20,
        pageList: [10, 20, 50, 100],
        clickToSelect: true,
        locale: "zh-CN",
        striped: true,
        toggle: true,
        silent: true,
        sidePagination: "server",
        queryParams: function (params) {
            var temp = {
                username: $("input[name='username']").val(),
                companyname: $("input[name='companyname']").val(),
                clerkname: $("input[name='clerkname']").val(),
                startDate: startDate,
                endDate: endDate,
                offset: params.offset,
                limit: params.limit,
                sort: params.sort,
                sortOrder: params.order
            };
            // 设置开始时间
            $("#startDate").val(temp.startDate);
            // 设置结束时间
            $("#endDate").val(temp.endDate);
            return temp;
        },
        columns: colums,
        // data:data,
        onLoadSuccess: function (result) {
            // 设置导出limit值
            if (data.total) {
                $("#limit").val(result.total);
            }
        },
        onLoadError: function (error) {
            console.log(error);
        },
        onDblClickRow: function (row, $element) {
        }
    });
}
