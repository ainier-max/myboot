/**
* 需求描述：api组件
* @author chenbc 2019-09-23
*/
<template>
  <div>

  </div>
  <!-- export default { -->
</template>

<script>
    module.exports = {
        name:"ApiComponent",
        props: {
          axios:{type: Function},
        },
        data() {
            return {
              //请求头
              headers_config: {
                  headers: {
                    "Content-Type":"application/json;charset=utf-8",
                    "dataType":"json"
                  }
              }
            }
        },
        computed: {
        },
        watch: {

        },
        components: {},
        methods: {
          /**
           * 地址正向匹配查询
           */
           addressPositive(param,cb) {
            param.count=param.count+"";
            this.axios.post(window.gisConfig.serverUrl+'/cbc/positive/match.cbc',param,this.headers_config)
              .then(response => {
                cb(response);
              }).catch(error => {
                console.log(error);
            });
           },
          /**
           * 地址反向匹配查询
           */
          addressReverse(param,cb) {
            param.lat=param.lat+"";
            param.lng=param.lng+"";
            param.count=param.count+"";
            param.radius=param.radius+"";
            this.axios.post(window.gisConfig.serverUrl+'/cbc/reverse/match.cbc',param,this.headers_config)
              .then(response => {
                cb(response);
              }).catch(error => {
              console.log(error);
            });
          },
          /**
           * 线周边查询
           */
          arroundPolyline(param,cb) {
            param.count=param.count+"";
            param.radius=param.radius+"";
            this.axios.post(window.gisConfig.serverUrl+'/cbc/polyline/buffer.cbc',param,this.headers_config)
              .then(response => {
                cb(response);
              }).catch(error => {
              console.log(error);
            });
          },
          /**
           * 多边形空间查询
           */
          spacePolygon(param,cb) {
            param.count=param.count+"";
            this.axios.post(window.gisConfig.serverUrl+'/cbc/space/polygon.cbc',param,this.headers_config)
              .then(response => {
                cb(response);
              }).catch(error => {
              console.log(error);
            });
          },
          /**
           * 矩形形空间查询（按照多边形的方法）
           */
          spaceRect(param,cb) {
            param.count=param.count+"";
            this.axios.post(window.gisConfig.serverUrl+'/cbc/space/polygon.cbc',param,this.headers_config)
              .then(response => {
                cb(response);
              }).catch(error => {
              console.log(error);
            });
          },
          /**
           * 圆空间查询
           */
          spaceCircle(param,cb) {
            param.lat=param.lat+"";
            param.lng=param.lng+"";
            param.count=param.count+"";
            param.radius=param.radius+"";
            this.axios.post(window.gisConfig.serverUrl+'/cbc/space/circle.cbc',param,this.headers_config)
              .then(response => {
                cb(response);
              }).catch(error => {
              console.log(error);
            });
          },
          /**
           * 获取行政区划信息
           */
          getRegionInfo(param,cb){
            param.id=param.regioncode+"";
            param.sql="mysql_zzjg.zzjg_info";
            var the=this;
            this.axios.post(window.gisConfig.serverUrl+'/cbc/select.cbc',param,this.headers_config)
              .then(response => {
                  cb(response);
              }).catch(error => {
              console.log(error);
            });
          },
            /**
             * 获取行政区划树信息
             */
            getRegionTreeInfo(param,cb){
                param.id=param.regioncode+"";
                param.sql="mysql_zzjg.zzjg_tree";
                var the=this;
                this.axios.post(window.gisConfig.serverUrl+'/cbc/select.cbc',param,this.headers_config)
                    .then(response => {
                        cb(response);
                    }).catch(error => {
                    console.log(error);
                });
            },

            /**
             * wkt坐标格式转geojson格式
             */
            wktToGeojson(param,cb){
                param.zbc=param.zbc+"";
                var the=this;
                this.axios.post(window.gisConfig.serverUrl+'/cbc/wkt/to/geojson.cbc',param,this.headers_config)
                    .then(response => {
                        response.param=param;
                        cb(response);
                    }).catch(error => {
                        console.log(error);
                    }
                );
            },



          /**
           * 判断点与行政区域的关系
           */
          judgeRegionByXY(param,cb){
            param.lat=param.lat+"";
            param.lng=param.lng+"";
            param.regioncode=param.regioncode+"";
            this.axios.post(window.gisConfig.serverUrl+'/cbc/judge/region/xy.cbc',param,this.headers_config)
              .then(response => {
                cb(response);
              }).catch(error => {
              console.log(error);
            });
          },



          /**
           * 米转度
           * @param meter
           */
          meterToDegree(meter){
            var degree=meter/(1000*101.11);
            return degree;
          },
          /**
           * 度转米
           * @param meter
           */
          degreeToMeter(degree){
            var meter=(1000*101.11)*degree;
            return meter;
          },

          judgeCircleByXY(lat,lng,circle){
            var radiusMeter=this.meterToDegree(circle.radius);
            var judgeFlag=this.pointlnCicle(lng,lat,circle.centerPoint.lng,circle.centerPoint.lat,radiusMeter);
            return judgeFlag;
          },
          /**
           * 判断后坐标是否在圆内
           * @param x1:圆心x坐标
           * @param y1:圆心Y坐标
           * @param x2:X坐标
           * @param y2:Y坐标
           * @param r:半径
           * @returns {boolean}
           * @constructor
           */
          pointlnCicle(x1,y1,x2,y2,r){
            var xdi=x2-x1;
            var ydi=y2-y1;
            if(Math.pow((xdi*xdi+ydi*ydi),0.5)<r){
              return true;
            }else{
              return false;
            }
          },
          /**
           * 判断点与矩形的关系
           */
          judgeRectByXY(lat,lng,rect){
            //console.log("rect",rect);
            var pointxy=lng+","+lat;
            var polygonxy="";
            for(var i=0;i<rect.zbc.length;i++){
              polygonxy=polygonxy+rect.zbc[i][1]+","+rect.zbc[i][0]+","
            }
            polygonxy=polygonxy.substr(0,polygonxy.length-1);
            //console.log("polygonxy:",polygonxy);
            var judgeFlag=this.pointInPolygon(pointxy,polygonxy);
            return judgeFlag;
          },

          /**
           * 判断点与矩形的关系
           */
          judgePolygonByXY(lat,lng,polygon){
            console.log("polygon",polygon);
            var pointxy=lng+","+lat;
            var polygonxy="";
            for(var i=0;i<polygon.zbc[0].length;i++){
              polygonxy=polygonxy+polygon.zbc[0][i][1]+","+polygon.zbc[0][i][0]+","
            }
            polygonxy=polygonxy.substr(0,polygonxy.length-1);
            console.log("polygonxy:",polygonxy);
            var judgeFlag=this.pointInPolygon(pointxy,polygonxy);
            return judgeFlag;
          },
          /**
           * 判断后坐标是否在面内
           * @param point 119.28834,26.08525
           * @param polygon 119.28914308547974,26.086392402648926,119.28661108016968,26.08407497406006,119.28952932357788,26.08379602432251,119.28914308547974,26.086392402648926
           */
          pointInPolygon(point, polygon){
            var pArr = point.split(",");
            var p =  new Object();
            p.x = parseFloat(pArr[0]);
            p.y = parseFloat(pArr[1]);
            var pt = new Array();
            var gArr = polygon.split(",");
            var num = gArr.length;
            for(var i=0;i<num;i++){
              var p1 =  new Object();
              p1.x = parseFloat(gArr[i]);
              p1.y = parseFloat(gArr[++i]);
              pt.push(p1);
            }
            var nCount = pt.length;
            var nCross = 0;
            for (var i = 0; i < nCount; i++)
            {
              var p1 = pt[i];
              var p2 = pt[(i + 1) % nCount];
              if (p1.y == p2.y)
              {
                if (p.y == p1.y && p.x >= this.getMin(p1.x, p2.x) && p.x <= this.getMax(p1.x, p2.x))
                  return true;
                continue;
              }
              if (p.y < this.getMin(p1.y, p2.y) || p.y > this.getMax(p1.y, p2.y))
                continue;
              var x = (p.y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y) + p1.x;
              if (x > p.x)
                nCross++;
              else if (x == p.x)
                return true;
            }
            if (nCross % 2 == 1)
              return true;
            return false;
          },
          /**
           * 获取较小值
           * @param x
           * @param y
           * @returns {*}
           */
          getMin(x, y){
            if(x>y){
              return y;
            }else{
              return x;
            }
          },
          /**
           * 获取较大值
           * @param x
           * @param y
           * @returns {*}
           */
          getMax(x, y){
            if(x<y){
              return y;
            }else{
              return x;
            }
          },

        },
        beforeCreate() {
        },
        created() {
        },
        beforeMount(){},
        mounted(){
        },
        beforeUpdate(){},
        updated(){},
        beforeDestroy() {},
        destroyed(){}
    }
</script>

<style>

</style>
