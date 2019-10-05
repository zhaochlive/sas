function loadTable(){
    var keys=null;
    var colums=[];
    var data=[];

    colums.push(
        {field: 'clerkname',title: '客服名称', sortable: false ,align: 'center'},
        {field: 'waysalesman',title: '业务员名称', sortable: false ,align: 'center'},
        {field: 'realname',title: '客户名称', sortable: false ,align: 'center'},
        {field: 'mobile',title: '手机号', sortable: false ,align: 'center'},
        {field: 'companyname',title: '客户公司名称', sortable: false,align: 'center' ,width:500},
        {field: '总下单量',title: '总下单量', sortable: true ,align: 'center'},
        {field: 'firstTime',title: '首次下单时间', sortable: true ,align: 'center',formatter: function (value, row, index) {
                var date = row.firsttime;
                date = date.replace("T", " ");
                return date.substring(0, date.indexOf('.'));
            }}
        );

    $.ajax({
        url: "repurchase/getColums",
        type: "post",
        async: false,
        // data: data,
        data: {
            startDate: startDate,
            endDate: endDate
        },
        dataType: "json",
        success: function (resultData) {
            keys=resultData;
            if(resultData){
                $.each(resultData,function(index,value){
                    colums.push({field:value,title:value,sortable: true ,align: 'center'});
                });
            }
        },
    });

    initTable(colums,data);
}

function initTable(colums,data){

    $("#dataTable table").bootstrapTable('destroy').bootstrapTable({
        url: "/repurchase/getRepurchaseRate",
        method: "get",
        cache: false,
        clickToSelect: true,
        pagination: true,
        sortable: true,
        // sortName:"name",
        // sortOrder: "desc",     //排序方式
        sortName: "总下单量",
        sortOrder: "DESC",
        height: 580,
        pageNumber: 1,
        pageSize: 100,
        pageList: [10, 20, 50, 100],
        locale: "zh-CN",
        striped: true,
        toggle: true,
        // smartDisplay: false,
        silent: true,
        sidePagination: "server",
        queryParams: function (params) {
            var temp = {
                username: $("input[name='username']").val(),
                companyname: $("input[name='companyname']").val(),
                mobile: $("input[name='mobile']").val(),
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
        columns:colums,
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
