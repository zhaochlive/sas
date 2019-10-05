//获取当月第一天
function firstDayOfMonthSt() {
    var now = new Date();
    return now.getFullYear()+"-"+ (now.getMonth()+1)+"-"+1;
}
function firstDayOfMonth() {
    var now = new Date();
    return new Date(now.getFullYear(),now.getMonth(),1);
}
//获取当月最后一天
function endDayOfMonth() {
    var now = new Date();
    return new Date(now.getFullYear(),now.getMonth()+1,0);
}
//获取当月最后一天
function endDayOfMonthSt() {
    var now = new Date();
    return now.getFullYear()+"-"+(now.getMonth()+1,0);
}

//获取当天时间
function today() {
    var now = new Date();
    return new Date(now.getFullYear(),now.getMonth()+1,now.getDate());
}
//获取当天
function todaySt() {
    var now = new Date();
    return now.getFullYear()+"-"+ (now.getMonth()+1)+"-"+now.getDate();
}





