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
//
/**
 * @description 时区时间转化   +8小时
 * example:2019-10-27T16:00:00.000+0000 --> 2019-10-28 00:00:00
 * @param dateTime
 * @returns {string}
 */
function datePaseToString (dateTime) {
    if (dateTime==null||dateTime=='undefined'||dateTime==''||dateTime=='null'){
        return '';
    }
    dateTime = new Date(dateTime);
    var format = 'yyyy-MM-dd HH:mm:ss';
    var o = {
        'M+' : dateTime.getMonth() + 1, //month
        'd+' : dateTime.getDate(), //day
        'H+' : dateTime.getHours(), //hour小时
        'm+' : dateTime.getMinutes(), //minute
        's+' : dateTime.getSeconds(), //second
        'q+' : Math.floor((dateTime.getMonth() + 3) / 3), //quarter
        'S' : dateTime.getMilliseconds() //millisecond
    };
    if (/(y+)/.test(format))
        format = format.replace(RegExp.$1, (dateTime.getFullYear() + '').substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp('(' + k + ')').test(format))
            format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length));
    return format;

}



