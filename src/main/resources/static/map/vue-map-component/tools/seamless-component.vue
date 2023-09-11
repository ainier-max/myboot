<template>
  <div style="width:400px;height:250px;background-color: white;padding: 20px">
    <span style="color:red;font-size: 12px">*一次初始化只能采集一个面*</span><br>
    <span style="color:red;font-size: 12px">*重新调用init方法即可采集新数据*</span><br>
    <span style="color:red;font-size: 12px">*新区域采集方向:逆时针*</span>
    <el-divider></el-divider>
    <span style="color:gray;font-size: 12px">1.创建新区域，从地图上选一个点，采集坐标。</span><br>
    <el-button type="primary" style="margin-top: 10px" @click='addNewPoint' size="mini">
      新区域增新点
    </el-button>
    <el-divider style="margin:10px 0"></el-divider>
    <span style="color:gray;font-size: 12px">2.从地图上已存在的点作为新区域的采集坐标。</span><br>
    <el-form :inline="true" style="padding-top: 10px" class="demo-form-inline">
      <el-form-item>
        <el-button type="primary" @click="addOldPoint" size="mini">新区域新增已存在点</el-button>
      </el-form-item>
      <el-form-item>
        <el-input v-model="xh" placeholder="点序号" size="mini"></el-input>
      </el-form-item>
    </el-form>


    <div v-show="seamlessPopupFlag" ref="seamlessPopupRef" style="width: 150px;height: 130px;">
      起点：
      <el-select size="mini" v-model="startPointIndex" @change="startChange" placeholder="请选择">
        <el-option
          v-for="(item,index)  in startPointIndexs"
          :key="index"
          :label="item.value"
          :value="item.value">
        </el-option>
      </el-select>
      终点：
      <el-select size="mini" disabled v-model="endPointIndex" placeholder="请选择">
        <el-option
          v-for="(item,index) in endPointIndexs"
          :key="index"
          :label="item.value"
          :value="item.value">
        </el-option>
      </el-select>
      <div align="center" style="padding-top:10px">
        <el-button type="primary" size="mini" @click="addBreakPoint">新增折点</el-button>
      </div>
    </div>

  </div>
</template>

<script>
    module.exports = {
        name: "SeamlessComponent",
        props: {
            mapRef: {type: String, request: true},//地图引用id
        },
        data() {
            return {
                //地图对象
                map: null,
                //点下标值
                indexFlag: 0,
                //点数组
                markers: [],
                //面数组
                polygons: [],
                //气泡框显示标识
                seamlessPopupFlag: false,
                //当前打开气泡框的面
                currentPolygon: null,
                //选择框起点集合
                startPointIndexs: [],
                //选择框终点集合
                endPointIndexs: [],
                //起点选中值
                startPointIndex: null,
                //终点选中值
                endPointIndex: null,
                //图标信息
                markerJSON: {},
                //新增区域
                newPolygon: null,
                //新增存在点序号
                xh: '',
                //回调方法
                callbackFun: '',
            }
        },
        methods: {
            init(sqs, fun) {
                //console.log("this.mapRef",this.mapRef);
                this.map = this.$parent.$refs[this.mapRef];
                if (typeof (this.map) == "undefined") {
                    window.setTimeout(function () {
                        this.init(sqs);
                    }, 500);
                } else {
                    //console.log("地图对象，",this.map);
                    this.showArea(sqs);
                    this.callbackFun = fun;
                }
            },
            //展示所有区域的边界范围与边界点
            showArea(sqs) {
                for (var j = 0; j < sqs.length; j++) {
                    var data = sqs[j];
                    var polygonJSON = {};
                    polygonJSON.xys = [data.zbc];
                    polygonJSON.option = {};
                    polygonJSON.option.weight = 2;
                    polygonJSON.option.color = "#0000FF";
                    polygonJSON.option.fillColor = "#0000FF";
                    polygonJSON.option.fillOpacity = 0.5;
                    var polygon = this.map.addPolygon(polygonJSON);
                    polygon.dm = data.dm;
                    polygon.indexs = [];
                    for (var i = 0; i < data.zbc.length; i++) {
                        var flagObj = {};
                        //console.log("this.markers.length",this.markers.length);
                        for (var k = 0; k < this.markers.length; k++) {
                            //相同点只上一个点(点已存在地图上)
                            //console.log("data.zbc[i][0]",data.zbc[i][0]);
                            //console.log("this.markers[k].xy[0]",this.markers[k]._latlng);
                            if (this.markers[k]._latlng.lat == data.zbc[i][0] && this.markers[k]._latlng.lng == data.zbc[i][1]) {
                                flagObj.flag = true;
                                flagObj.index = this.markers[k].index;
                            }
                        }
                        //console.log("flag:",flagObj);
                        if (flagObj.flag == true) {
                            var indexObj = {};
                            indexObj.indexFlag = flagObj.index;
                            indexObj.xy = data.zbc[i];
                            polygon.indexs.push(indexObj);
                        } else {
                            this.indexFlag = this.indexFlag + 1;
                            this.markerJSON.xy = data.zbc[i];
                            var marker = this.map.addMarker(this.markerJSON);
                            marker.index = this.indexFlag;
                            var option = {direction: 'top', offset: [0, 0], permanent: true, opacity: 1, sticky: false};
                            var content = this.indexFlag + "";
                            this.map.bindTooltip(marker, content, option);

                            var indexObj = {};
                            indexObj.indexFlag = this.indexFlag;
                            indexObj.xy = data.zbc[i];
                            polygon.indexs.push(indexObj);

                            this.addEventMarkerDrag(marker);
                            this.markers.push(marker);
                        }

                    }
                    this.addEventPolygonClick(polygon);
                    this.polygons.push(polygon);

                }
            },
            //区域点击事件
            addEventPolygonClick(obj) {
                this.map.onLayerEvent("click", obj, this.polygonClickEventCallBack);
            },
            //区域点击回调方法
            polygonClickEventCallBack(e) {
                this.seamlessPopupFlag = true;
                this.currentPolygon = e.target;
                this.startPointIndexs = [];
                this.endPointIndexs = [];
                for (var i = 0; i < this.currentPolygon.indexs.length; i++) {
                    var obj = {};
                    obj.value = this.currentPolygon.indexs[i].indexFlag;
                    if (i != this.currentPolygon.indexs.length - 1) {
                        this.startPointIndexs.push(obj);
                    }
                    if (i != 0) {
                        this.endPointIndexs.push(obj);
                    }
                }
                var option = {closeOnClick: false, closeButton: true, offset: [0, 0], minWidth: 154};
                var point = [e.latlng.lat, e.latlng.lng];
                var html = this.$refs.seamlessPopupRef;
                this.map.addPopup(option, point, html);
            },

            startChange(value) {
                for (var i = 0; i < this.startPointIndexs.length; i++) {
                    if (this.startPointIndexs[i].value == value) {
                        this.startPointIndex = value;
                        this.endPointIndex = this.endPointIndexs[i].value;
                    }
                }
            },

            //面添加折点，判断是否为临近点
            addBreakPoint() {
                if (this.startPointIndex == null) {
                    this.$message.error("请选择起点！");
                    return;
                }
                console.log("开始采集");
                this.map.closeAllPopup();
                this.map.onClick(this.mapEventCallBack);
            },
            //地图点击回调方法
            mapEventCallBack(e) {
                this.map.closeAllPopup();
                //console.log("采集坐标：", e);
                this.map.offClick(this.mapEventCallBack);

                this.indexFlag = this.indexFlag + 1;
                this.markerJSON.xy = [e.latlng.lat, e.latlng.lng];
                var marker = this.map.addMarker(this.markerJSON);
                marker.index = this.indexFlag;
                var option = {direction: 'top', offset: [0, 0], permanent: true, opacity: 1, sticky: false};
                var content = this.indexFlag + "";
                this.map.bindTooltip(marker, content, option);

                var indexObj = {};
                indexObj.indexFlag = this.indexFlag;
                indexObj.xy = [e.latlng.lat, e.latlng.lng];

                for (var i = 0; i < this.currentPolygon.indexs.length; i++) {
                    var indexFlag = this.currentPolygon.indexs[i].indexFlag;
                    //console.log("indexFlag",indexFlag);
                    //console.log("this.startPointIndex",this.startPointIndex);
                    if (indexFlag == this.startPointIndex) {
                        this.currentPolygon.indexs.splice((i + 1), 0, indexObj);
                        console.log("this.currentPolygon.indexs", this.currentPolygon.indexs);
                    }
                }

                //console.log("this.polygons",this.polygons);
                for (var i = 0; i < this.polygons.length; i++) {
                    var polygon = this.polygons[i];
                    var newxys = [];
                    for (var j = 0; j < polygon.indexs.length; j++) {
                        newxys.push(polygon.indexs[j].xy);
                    }
                    polygon.setLatLngs(newxys);
                }


                this.addEventMarkerDrag(marker);
                this.markers.push(marker);

                //起点选中值
                this.startPointIndex = null;
                this.endPointIndex = null;
                this.callback();
            },
            //旧区域marker拖动回调事件
            addEventMarkerDrag(obj) {
                var the = this;
                //console.log("地图上所有的polygons,",the.polygons);
                obj.on('drag', function (e) {
                    for (var i = 0; i < the.polygons.length; i++) {
                        var polygon = the.polygons[i];
                        var newxys = [];
                        for (var j = 0; j < polygon.indexs.length; j++) {
                            if (polygon.indexs[j].indexFlag == e.target.index) {
                                newxys.push(e.latlng);
                                polygon.indexs[j].xy = [e.latlng.lat, e.latlng.lng];
                            } else {
                                newxys.push(polygon.indexs[j].xy);
                            }
                        }
                        polygon.setLatLngs(newxys);
                    }
                    //回调方法
                    the.callback();
                });
            },
            //新区域新增点
            addNewPoint() {
                this.map.onClick(this.addNewPointMapEventCallBack);
            },
            //新增区域的点,并画出区域
            addNewPointMapEventCallBack(e) {
                //画点
                var point = [e.latlng.lat, e.latlng.lng];
                this.map.offClick(this.addNewPointMapEventCallBack);
                this.indexFlag = this.indexFlag + 1;
                this.markerJSON.xy = point;
                var marker = this.map.addMarker(this.markerJSON);
                marker.index = this.indexFlag;
                var option = {direction: 'top', offset: [0, 0], permanent: true, opacity: 1, sticky: false};
                var content = this.indexFlag + "";
                this.map.bindTooltip(marker, content, option);
                this.addEventNewMarkerDrag(marker);
                this.markers.push(marker);

                var indexObj = {};
                indexObj.indexFlag = this.indexFlag;
                indexObj.xy = [e.latlng.lat, e.latlng.lng];
                //画区域
                if (this.newPolygon == null) {
                    var polygonJSON = {};
                    polygonJSON.xys = [point];
                    polygonJSON.option = {};
                    polygonJSON.option.weight = 2;
                    polygonJSON.option.color = "#3388ff";
                    polygonJSON.option.fillColor = "#3388ff";
                    polygonJSON.option.fillOpacity = 0.5;
                    this.newPolygon = this.map.addPolygon(polygonJSON);
                    this.newPolygon.indexs = [];
                    this.newPolygon.indexs.push(indexObj);
                    this.polygons.push(this.newPolygon);
                } else {
                    this.newPolygon.addLatLng(point);
                    this.newPolygon.indexs.push(indexObj);
                }
                //回调方法
                this.callback();
            },
            //新区域marker拖动回调事件
            addEventNewMarkerDrag(obj) {
                var the = this;
                //console.log("地图上所有的polygons,",the.polygons);
                obj.on('drag', function (e) {
                    var newxys = [];
                    for (var j = 0; j < the.newPolygon.indexs.length; j++) {
                        if (the.newPolygon.indexs[j].indexFlag == e.target.index) {
                            newxys.push(e.latlng);
                            the.newPolygon.indexs[j].xy = [e.latlng.lat, e.latlng.lng];
                        } else {
                            newxys.push(the.newPolygon.indexs[j].xy);
                        }
                    }
                    the.newPolygon.setLatLngs(newxys);
                    //回调方法
                    the.callback();
                });
            },
            //新增在地图上已存在的点
            addOldPoint() {
                if (this.newPolygon == null) {
                    this.$message.error("新区域暂未存在，无法添加！");
                    return;
                }
                //console.log("this.markers:",this.markers);
                var inFlag = false;
                for (var i = 0; i < this.markers.length; i++) {
                    var marker = this.markers[i];
                    if (this.xh == marker.index) {
                        var point = [marker._latlng.lat, marker._latlng.lng];

                        var indexObj = {};
                        indexObj.indexFlag = marker.index;
                        indexObj.xy = point;
                        this.newPolygon.indexs.push(indexObj);
                        this.newPolygon.addLatLng(point);
                        inFlag = true;
                        //采集完后的回调方法
                        this.callback();
                    }
                }
                if (inFlag == false) {
                    this.$message.error("输入的点序列在地图上不存在！");
                }

            },
            callback() {
                console.log("this.map", this.map.map.useType);
                if (this.map.map.useType == "js") {
                    window[this.callbackFun](this.polygons);
                } else {
                    this.$parent[this.callbackFun](this.polygons);
                }
            },
        },
        mounted() {
            this.markerJSON = {};
            this.markerJSON.iconUrl = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAD2SURBVFhHYxgFo2AUjIJRQDHwm6MOZdEJeExkZ/Ra2Mbkvfgcg/fC/zDM5LX4GEgcJA9VSQMA9C26xeiY2XfJNSDtDtVBReA7W5zZZ+EzdAtxYr9F5lCd1AGM3ou2YbUIBwaHBNWiAxSkWCwhjBfnQ02gEPgsqsduAX7M6LVoHdQEygDYICwWEMLgNEMNMOAOIDsKQAmXKoDMRAgumKgFGH0W7cNmCS7M7LXwHkPoTH6odioAUEHku/AdNsuwYp+F9lCdVATA0g1a1GK3FIihpSUNimIYgFVGwMoHxWKgw4Di06gb7MQA77n6UNYoGAWjgAzAwAAAqVZN4+c2VlcAAAAASUVORK5CYII=";
            this.markerJSON.iconAnchor = [16, 16];
            this.markerJSON.width = 32;
            this.markerJSON.height = 32;
            this.markerJSON.draggable = true;
        }
    }
</script>

<style scoped>
  .el-divider--horizontal {
    display: block;
    height: 2px;
    width: 100%;
    margin-top: 16px;
    margin-right: 0px;
    margin-bottom: 8px;
    margin-left: 0px;
  }
</style>
