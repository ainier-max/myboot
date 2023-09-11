/**
* 需求描述：gis条件组件
* 功能：提供poi条件查询功能
* 使用：
* @author cbc 2019-12-27
*/
<template>
  <div>
    <!--查询区域-->
    <el-row style="width: 500px">
      <!--区域选择-->
      <el-col :span="5">
        <div>
          <div>
            <el-button type="primary" @click="openRegion" style="padding-right: 10px">
              {{regionObj.name}}<i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
          </div>
        </div>
      </el-col>
      <el-col :span="14">
        <div>
          <div>
            <el-input v-model="keyword" @focus="openLayer" placeholder="请输入关键字"></el-input>
          </div>
        </div>
      </el-col>
      <el-col :span="5" style="padding-left: 10px">
        <!--        <el-button type="primary" @click="search()" icon="el-icon-search">搜索</el-button>-->
        <el-button type="danger" @click="clearAll()" icon="el-icon-delete">清空</el-button>
      </el-col>
    </el-row>

    <el-row style="width: 500px;padding-top: 5px">
      <!--区域选择-->
      <div style="background-color: white;width: 500px;" v-show="regionFlag">
        <ul style="padding-inline-start:10px;padding-top: 5px;padding-right: 5px">
          <el-divider content-position="left">区域选择</el-divider>
<!--          <div style="position: absolute;right: 5px;top:9px;cursor: pointer;color:gray" @click="closeRegion"-->
<!--               title="关闭">x-->
<!--          </div>-->
          <li v-if="childRegion.length==0"
              style="font-size:12px;list-style:none;color:red;font-weight: bold;padding-left: 20px">
            无下级机构
          </li>
          <li v-else v-for="(item,i) in childRegion"
              style="cursor:pointer;padding-left:10px;padding-top:5px;display:inline-block;list-style:none;">
            <el-button style="opacity: 0.5" type="primary" size="mini" @click="setRegion(item.id)" round>
              {{item.name}}
            </el-button>
          </li>
        </ul>
        <div style="padding-left: 20px;padding-bottom: 10px;padding-right: 20px;padding-top: 10px">
          <el-button type="info" size="mini" @click="returnRegion">回上级</el-button>
        </div>
      </div>
      <!--图层选择-->
      <div style="background-color: white;width: 500px;" v-show="keywordFlag">
        <ul style="padding-inline-start:10px;padding-top: 5px;padding-right: 5px">
          <el-divider content-position="left">图层选择</el-divider>
<!--          <div style="position: absolute;right: 5px;top:9px;cursor: pointer;color:gray" @click="closeLayer"-->
<!--               title="关闭">x-->
<!--          </div>-->
          <li v-for="(item,i) in layers"
              style="cursor:pointer;padding-left:10px;padding-top:5px;display:inline-block;list-style:none;">
            <el-button v-if="item.checked==true" style="opacity: 1" type="primary" size="mini"
                       @click="setLayerNotChecked(item.type)" round>
              {{item.name}}
            </el-button>
            <el-button v-else style="opacity: 0.2" type="warning" size="mini" @click="setLayerChecked(item.type)"
                       round>
              {{item.name}}
            </el-button>
          </li>
        </ul>
        <ul style="padding-inline-start:10px;padding-top: 5px;padding-right: 5px">
          <el-divider content-position="left" style="margin-top:-20px">工具选择</el-divider>
          <li style="cursor:pointer;padding-left:10px;padding-top:5px;display:inline-block;list-style:none;">
            <el-popover
              placement="top"
              width="50"
              v-model="visible">
              <el-row>
                <el-col :span="20">
                  <el-input v-model="arroundDistance" size="mini" placeholder="周边距离"></el-input>
                </el-col>
                <el-col :span="4">
                  <div style="padding-top: 3px">米</div>
                </el-col>
              </el-row>
              <div style="text-align: center; margin: 0;padding-top: 5px">
                <el-button size="mini" type="text" @click="visible = false">取消</el-button>
                <el-button type="primary" size="mini" @click="draw('polyline')">确定</el-button>
              </div>
              <el-button size="mini" slot="reference" round>线</el-button>
            </el-popover>
          </li>
          <li style="cursor:pointer;padding-left:10px;padding-top:5px;display:inline-block;list-style:none;">
            <el-button size="mini" @click="draw('circle')" round>圆</el-button>
          </li>
          <li style="cursor:pointer;padding-left:10px;padding-top:5px;display:inline-block;list-style:none;">
            <el-button size="mini" @click="draw('rect')" round>矩形</el-button>
          </li>
          <li style="cursor:pointer;padding-left:10px;padding-top:5px;display:inline-block;list-style:none;">
            <el-button size="mini" @click="draw('polygon')" round>多边形</el-button>
          </li>
        </ul>

        <div style="height: 10px;"></div>
      </div>
    </el-row>

    <el-row style="width: 500px;padding-top: 10px" v-if="layerResultFlag">
      <el-collapse v-model="activeNames" @change="layerResultHandleChange" accordion>
        <div v-for="(item,i) in layerResults">
          <el-collapse-item :title="item.name+'('+item.result.length+')'" :name="item.type">
            <div style="overflow: auto;max-height: 500px">
              <div v-for="(obj,i) in item.result" style="padding-bottom: 15px">
                <div @mouseover="mouseOver($event)"
                     @mouseleave="mouseLeave($event)"
                     style="padding-left: 20px;padding-bottom: 10px;cursor: pointer"
                     @click="toPosition(obj)"
                >
                  <div style="float: left;padding-top: 20px;padding-right: 10px"><img width="32px" height="32px"
                                                                                      :src="item.iconUrl"></div>
                  <div>
                    <div style="color:blue;padding-top: 10px">{{obj.abbrname}}</div>
                    <!--<div style="padding-top: 2px">{{obj.lng}},{{obj.lat}}</div>-->
                    <div>{{obj.address}}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </div>
      </el-collapse>
    </el-row>
  </div>
  <!-- export default { -->
</template>

<script>
    module.exports = {
        name: "PoiComponent",
        props: {
            mapRef: {type: String, request: true},//地图引用id
            mapApiRef: {type: String, request: true},//地图api引用id
            regionCode: {type: String},//组织机构代码
            word: {type: String},//组织机构代码
            total: {type: String},//组织机构代码
            axios: {type: Function},
        },
        data() {
            return {
                //请求头
                headers_config: {
                    headers: {
                        "Content-Type": "application/json;charset=utf-8",
                        "dataType": "json"
                    }
                },
                map: null, //地图对象
                mapApi: null, //地图查询api
                regionFlag: false,//区域组件选择窗
                regionObj: {},//当前区域对象数据
                regionTreeStr: "",//行政机构树（350202,350203）
                childRegion: [],//子组织机构
                layers: [],//图层选择
                layersStr: '',//选中的图层（101005,101007）
                layersMap: null,
                keyword:this.word,
                keywordFlag: false,//关键字窗口
                activeNames: '',
                layerResultFlag: false,
                layerResults: [],
                //地图上的点数据组
                makerLayers: [],
                //地图画的图形
                drawLayers: [],
                //列表返回条数
                count: Number(this.total),
                //线周边距离设置窗口
                visible: false,
                //线周边距离
                arroundDistance: 500,
                //首次值变化不查询
                keywordFlag:0,
                regionTreeStrFlag:0,
                layersStrFlag:0,
            }
        },
        watch: {
            keyword: {
                handler(newVal, oldVal) {
                    if(this.keywordFlag==0){
                        this.keywordFlag=1;
                    }else{
                        this.search();
                    }
                },
                deep: true
            },
            regionTreeStr:{
                handler(newVal, oldVal) {
                    if(this.regionTreeStrFlag==0){
                        this.regionTreeStrFlag=1;
                    }else{
                        this.search();
                    }
                },
                deep: true
            },
            layersStr:{
                handler(newVal, oldVal) {
                    if(this.layersStrFlag==0){
                        this.layersStrFlag=1;
                    }else{
                        this.search();
                    }
                },
                deep: true
            },
            layerResults:{
                handler(newVal, oldVal) {
                    //地图绑定移除事件监听
                    this.map.onUnload(this.onUnload);
                },
                deep: true
            },
        },
        methods: {
            /**
             * 初始化地图与poi查询组件
             */
            initPoiSearch() {
                this.map = this.$parent.$refs[this.mapRef];
                this.mapApi = this.$parent.$refs[this.mapApiRef];
                if (typeof (this.map) === "undefined" || typeof (this.mapApi) === "undefined") {
                    window.setTimeout(this.initPoiSearch, 500);
                } else {
                    //根据传入的区域查询相关其区域信息
                    var param = {};
                    param.regioncode = this.regionCode;
                    this.mapApi.getRegionInfo(param, this.getRegionInfoBack);
                    //获取组织机构树
                    this.getChildRegionTree(this.regionCode);
                    this.layers = window.gisConfig.layerConfig;
                    //获取图层类别（字符串格式）
                    this.getLayersStr();
                    //图层参数值获取
                    this.layersMap = new Map();
                    for (var i = 0; i < window.gisConfig.layerConfig.length; i++) {
                        var layerConfig = window.gisConfig.layerConfig[i];
                        var obj = {};
                        obj.checked = layerConfig.checked;
                        obj.name = layerConfig.name;
                        obj.iconUrl = layerConfig.iconUrl;
                        obj.iconAnchor = layerConfig.iconAnchor;
                        obj.width = layerConfig.width;
                        obj.height = layerConfig.height;
                        this.layersMap.set(layerConfig.type, obj);
                    }
                }
            },
            onUnload(){
                console.log("触发地图移除事件");
                this.layerResultFlag=false;
                this.layerResults=[];
                this.makerLayers=[];
                this.drawLayers=[];
            },
            getRegionInfoBack(resp) {
                console.log("行政区划查询结果:", resp);
                this.regionObj = resp.data[0].objects[0];
            },
            //获取组织机构树
            getChildRegionTree(pid) {
                var the = this;
                var param = {};
                param.id = pid + "";
                param.sql = "mysql_zzjg.zzjg_tree";
                this.axios.post(window.gisConfig.serverUrl + '/cbc/select.cbc', param, this.headers_config)
                    .then(response => {
                        //console.log("获取组织机构树：", response);
                        var regionTreeObjs = response.data[0].objects;
                        the.regionTreeStr = "";
                        for (var i = 0; i < regionTreeObjs.length; i++) {
                            the.regionTreeStr = the.regionTreeStr + regionTreeObjs[i].id + ",";
                        }
                        the.regionTreeStr = the.regionTreeStr.substr(0, the.regionTreeStr.length - 1);
                        console.log("获取组织机构树:", the.regionTreeStr);
                    }).catch(error => {
                    console.log(error);
                });
            },
            //获取子组织机构
            getChildRegion(pid) {
                //获取组织机构树
                this.getChildRegionTree(pid);
                var the = this;
                var param = {};
                param.sql = "mysql_zzjg.zzjg_child";
                param.pid = pid;
                //获取子组织机构
                this.axios.post(window.gisConfig.serverUrl + '/cbc/select.cbc', param, this.headers_config)
                    .then(response => {
                        console.log("获取子组织机构：", response);
                        the.childRegion = response.data[0].objects;
                        the.regionFlag = true;
                        the.keywordFlag = false;
                    }).catch(error => {
                    console.log(error);
                });
            },
            //打开区域选择窗口
            openRegion() {
                this.getChildRegion(this.regionObj.id);
            },
            //关闭区域选择窗口
            closeRegion() {
                this.regionFlag = false;
            },
            //打开图层选择窗口
            openLayer() {
                this.keywordFlag = true;
                this.regionFlag = false;
            },
            //关闭图层选择窗口
            closeLayer() {
                this.keywordFlag = false;
            },
            //点击选择框中的机构按钮
            setRegion(id) {
                var param = {};
                param.regioncode = id;
                this.mapApi.getRegionInfo(param, this.getRegionInfoBack);
                this.getChildRegion(id);
            },
            //返回上一级机构
            returnRegion() {
                //console.log("regionObj:",this.regionObj);
                var pid = this.regionObj.pid;
                if (pid == null || pid == "") {
                    this.$message.error('查询不到上级机构！');
                    return;
                }
                var param = {};
                param.regioncode = pid;
                this.mapApi.getRegionInfo(param, this.getRegionInfoBack);
                this.getChildRegion(pid);
            },
            //图层选中
            setLayerChecked(type) {
                this.layersStr="";
                for (var i = 0; i < this.layers.length; i++) {
                    if (this.layers[i].type == type) {
                        this.layers[i].checked = true;
                    }
                }
                this.getLayersStr();
            },
            //图层不选中
            setLayerNotChecked(type) {
                this.layersStr="";
                for (var i = 0; i < this.layers.length; i++) {
                    if (this.layers[i].type == type) {
                        this.layers[i].checked = false;
                    }
                }
                this.getLayersStr();
            },
            getLayersStr() {
                for (var i = 0; i < this.layers.length; i++) {
                    if (this.layers[i].checked == true) {
                        this.layersStr = this.layersStr + this.layers[i].type + ",";
                    }
                }
                this.layersStr = this.layersStr.substr(0, this.layersStr.length - 1);
            },
            clearLayer() {
                //清空地图上的点
                for (var i = 0; i < this.makerLayers.length; i++) {
                    this.map.removeLayer(this.makerLayers[i]);
                }
                this.makerLayers = [];
                //清空地图上画的图形
                for (var i = 0; i < this.drawLayers.length; i++) {
                    this.map.removeLayer(this.drawLayers[i]);
                }
                this.drawLayers = [];
            },
            //清空
            clearAll(){
                //this.keyword="";
                //清空列表数据
                this.layerResults = [];
                this.layerResultFlag = false;
                //清空地图上的数据
                this.clearLayer();
                //隐藏图层选择窗口
                this.keywordFlag = false;
                //隐藏区域选择窗口
                this.regionFlag = false;
            },
            //查询
            search() {
                //清空列表数据
                this.layerResults = [];
                this.layerResultFlag = false;
                //清空地图上的数据
                this.clearLayer();
                var param = {};
                param.regioncode = this.regionTreeStr;
                if(this.layersStr!=""){
                    param.type = this.layersStr;
                }else{
                    param.type = "无效类型";
                }
                param.keyword = this.keyword;
                param.count = this.count;
                this.mapApi.addressPositive(param, this.callBack);
            },
            callBack(resp) {
                this.layerResults = [];
                const mapData = new Map();
                //结果上图
                var result = resp.data[0].objects;
                console.log("查询结果", result);
                //console.log("this.layers", this.layers);
                for (var i = 0; i < result.length; i++) {
                    var obj = result[i];
                    for (var j = 0; j < this.layers.length; j++) {
                        if (this.layers[j].checked == true && this.layers[j].type == obj.type) {
                            if (typeof (mapData.get(obj.type)) == "undefined") {
                                var array = new Array();
                                array.push(obj);
                                array.name = this.layers[j].name;
                                mapData.set(obj.type, array);
                                break;
                            } else {
                                mapData.get(obj.type).push(obj);
                                break;
                            }
                        }
                    }
                    this.addPoiMarker(obj);
                }
                mapData.forEach((item, key) => {
                    var obj = {};
                    obj.type = key;
                    obj.result = item;
                    obj.name = item.name;
                    obj.iconUrl = this.layersMap.get(key).iconUrl;
                    this.layerResults.push(obj);
                });
                if (this.layerResults.length > 0) {
                    this.activeNames = this.layerResults[0].type;
                }
                console.log("this.layerResults", this.layerResults);
                if (this.layerResults.length > 0) {
                    this.layerResultFlag = true;
                }
            },
            addPoiMarker(obj) {
                var markerJSON = {};
                markerJSON.xy = [Number(obj.lat), Number(obj.lng)];
                markerJSON.iconUrl = this.layersMap.get(obj.type).iconUrl;
                markerJSON.iconAnchor = this.layersMap.get(obj.type).iconAnchor;
                markerJSON.width = this.layersMap.get(obj.type).width;
                markerJSON.height = this.layersMap.get(obj.type).height;
                var marker = this.map.addMarker(markerJSON);
                var option = {};
                option.offset = [0, -this.layersMap.get(obj.type).iconAnchor[1] / 2];
                marker.html = "<div>" + obj.fullname + "</div>";
                marker.id = obj.type + obj.id;
                marker.lat = obj.lat;
                marker.lng = obj.lng;
                this.map.bindPopup(marker, option);
                this.makerLayers.push(marker);
            },
            draw(type) {
                //清空列表数据
                this.layerResults = [];
                this.layerResultFlag = false;
                //清空地图上的数据
                this.clearLayer();
                if (type == "polyline") {
                    this.visible = false;
                    this.map.drawPolyline(this.polylineReceiver);
                } else if (type == "circle") {
                    this.map.drawCircle(this.circleReceiver);
                } else if (type == "rect") {
                    this.map.drawRect(this.rectReceiver);
                } else if (type == "polygon") {
                    this.map.drawPolygon(this.polygonReceiver);
                }
            },
            polylineReceiver(polylineObj) {
                console.log("polylineObj:", polylineObj);
                this.drawLayers.push(polylineObj);
                var param = {};
                param.regioncode = this.regionTreeStr;
                if(this.layersStr!=""){
                    param.type = this.layersStr;
                }else{
                    param.type = "无效类型";
                }
                param.keyword = this.keyword;
                param.count = this.count;
                param.points = polylineObj.zbc;
                param.radius = this.arroundDistance + "";//单位米
                this.mapApi.arroundPolyline(param, this.polylineReceiverCallBack);
            },
            polylineReceiverCallBack(resp) {
                this.layerResults = [];
                console.log("线周边查询结果：", resp.data[0]);
                var result = resp.data[0].objects;
                var polylineBuffer = resp.data[0].polylineBuffer;
                //缓冲区
                var newPolylineBuffers = new Array();
                for (var j = 0; j < polylineBuffer.length; j++) {
                    newPolylineBuffers.push(polylineBuffer[j])
                }
                var polygonJSON = {};
                polygonJSON.xys = newPolylineBuffers;
                polygonJSON.option = {};
                polygonJSON.option.weight = 1;
                polygonJSON.option.color = "#00BFFF";
                polygonJSON.option.fillColor = "#3388ff";
                polygonJSON.option.fillOpacity = 0.3;
                var polygon = this.map.addPolygon(polygonJSON);
                console.log("polygon", polygon);
                this.drawLayers.push(polygon);

                const mapData = new Map();
                //结果上图
                //console.log("this.layers", this.layers);
                //结果上图
                for (var i = 0; i < result.length; i++) {
                    for (var j = 0; j < result[i].length; j++) {
                        var obj = result[i][j];
                        for (var k = 0; k < this.layers.length; k++) {
                            if (this.layers[k].checked == true && this.layers[k].type == result[i][j].type) {
                                if (typeof (mapData.get(obj.type)) == "undefined") {
                                    var array = new Array();
                                    array.push(obj);
                                    array.name = this.layers[k].name;
                                    mapData.set(obj.type, array);
                                    break;
                                } else {
                                    mapData.get(obj.type).push(obj);
                                    break;
                                }
                            }
                        }
                        this.addPoiMarker(obj);
                    }
                }
                mapData.forEach((item, key) => {
                    var obj = {};
                    obj.type = key;
                    obj.result = item;
                    obj.name = item.name;
                    obj.iconUrl = this.layersMap.get(key).iconUrl;
                    this.layerResults.push(obj);
                });
                if (this.layerResults.length > 0) {
                    this.activeNames = this.layerResults[0].type;
                }
                console.log("this.layerResults", this.layerResults);
                if (this.layerResults.length > 0) {
                    this.layerResultFlag = true;
                }
            },
            circleReceiver(circleObj) {
                console.log("circleObj",circleObj);
                this.drawLayers.push(circleObj);
                var param = {};
                param.regioncode = this.regionTreeStr;
                if(this.layersStr!=""){
                    param.type = this.layersStr;
                }else{
                    param.type = "无效类型";
                }
                param.lat = circleObj.centerPoint.lat;
                param.lng = circleObj.centerPoint.lng;
                param.radius = circleObj.radius;
                param.keyword = this.keyword;
                param.count = this.count;
                this.mapApi.spaceCircle(param, this.callBack);
            },
            rectReceiver(rectObj) {
                console.log("rectObj:", rectObj);
                this.drawLayers.push(rectObj);
                var param = {};
                param.regioncode = this.regionTreeStr;
                if(this.layersStr!=""){
                    param.type = this.layersStr;
                }else{
                    param.type = "无效类型";
                }
                param.keyword = this.keyword;
                param.count = this.count;
                param.points = [rectObj.zbc];//多边形是数组格式
                this.mapApi.spaceRect(param, this.callBack);
            },
            polygonReceiver(polygonObj) {
                console.log("polygonObj:", polygonObj);
                this.drawLayers.push(polygonObj);
                var param = {};
                param.regioncode = this.regionTreeStr;
                if(this.layersStr!=""){
                    param.type = this.layersStr;
                }else{
                    param.type = "无效类型";
                }
                param.keyword = this.keyword;
                param.count = this.count;
                param.points = [polygonObj.zbc];//多边形是数组格式
                this.mapApi.spacePolygon(param, this.callBack);
            },
            mouseOver(e) {
                //console.log(e.currentTarget);
                //console.log(e.target);
                e.currentTarget.style.background = "#f6f6f6";
            },
            mouseLeave(e) {
                e.currentTarget.style.background = "";
            },
            toPosition(obj) {
                for (var i = 0; i < this.makerLayers.length; i++) {
                    var id = obj.type + obj.id;
                    if (this.makerLayers[i].id == id) {
                        var point = [this.makerLayers[i].lat, this.makerLayers[i].lng];
                        this.map.panTo(point);
                        var option = {
                            closeOnClick: false,
                            closeButton: true,
                            offset: [0, -this.layersMap.get(obj.type).iconAnchor[1] / 2]
                        };
                        var point = point;
                        var html = "<div>" + obj.fullname + "</div>";
                        var popup = this.map.addPopup(option, point, html);
                        this.makerLayers.push(popup);
                    }
                }
            },
            //结果切换
            layerResultHandleChange(val) {

            },

        },
        mounted() {
            console.log("this.regionCode:", this.regionCode);
            this.initPoiSearch();
        }
    }
</script>
<style>
  .el-popover {
    min-width: 100px;
  }

  .el-divider--horizontal {
    display: block;
    height: 1px;
    width: 100%;
  }

  .el-collapse-item__header {
    background-color: #e0a1eb;
    padding-left: 10px;
    color: white;
  }

  .el-collapse-item__content {
    padding-bottom: 0px;
  }

  .el-collapse-item {
    border-bottom-color: white;
  }

</style>
