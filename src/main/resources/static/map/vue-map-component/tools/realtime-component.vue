
<template>
  <div>
  </div>
  <!--export default {-->
</template>

<script>
    window.realtimeArr=[];
    module.exports = {
        name:"RealtimeComponent",
        props: {
            mapRef: {type: String, request: true},//地图引用id
            realTime: {type: Number, default: 100},//实时时间设置
        },
        data() {
            return {
                map: null,
                realObjects:[],
            }
        },
        computed: {},
        watch: {
            realObjects: {
                handler(newVal, oldVal) {
                    console.log('深度监听-新数据', newVal);
                    //this.realObjects=newVal;
                    //this.addPulsingIcons(newVal);
                    for(var i=0;i<window.realtimeArr.length;i++){
                      for(var j=0;j<newVal.length;j++){
                        if(window.realtimeArr[i].id==newVal[j].id){
                            window.realtimeArr[i].setLatLng([newVal[i].y,newVal[i].x])
                        }
                      }
                    }
                    //新数据上图
                    for(var j=0;j<newVal.length;j++){
                        var flag=0;
                        for(var i=0;i<window.realtimeArr.length;i++){
                            if(window.realtimeArr[i].id==newVal[j].id){
                                flag=1;
                            }
                        }
                        if(flag==0){
                            this.addPulsingIcons([newVal[j]]);
                        }
                    }

                },
                deep: true
            }
        },
        components: {},
        methods: {
            /**
             * 初始化poi查询组件
             */
            open(objs) {
                window.realtimeArr=[];
                this.realObjects=objs;
                this.map = this.$parent.$refs[this.mapRef];
                if (typeof (this.map) === "undefined") {
                    window.setTimeout(this.initRealtimeComponent, 500);
                }
                if (this.realObjects.length=== "0") {
                    window.setTimeout(this.initRealtimeComponent, 500);
                }
                console.log("this.realObjects:",this.realObjects);
                this.addPulsingIcons(this.realObjects);
                console.log("realTime",this.realTime);
            },
            /**
             * 初始化poi查询组件
             */
            close() {
                for(var i=0;i<window.realtimeArr.length;i++){
                    this.map.removeLayer(window.realtimeArr[i]);
                }
                window.realtimeArr=[];
            },
            addPulsingIcons(objects) {
                for(var i=0;i<objects.length;i++){
                    var pulseJson={};
                    //xy坐标
                    pulseJson.xy=[objects[i].y,objects[i].x];
                    pulseJson.option={};
                    //波纹大小
                    pulseJson.option.iconSize=[10,10];
                    //波纹颜色
                    pulseJson.option.color='green';
                    //波纹圆点颜色
                    pulseJson.option.fillColor='green';
                    //波纹速度（数值越大，速度越慢）
                    pulseJson.option.heartbeat=2;
                    //是否显示波纹
                    pulseJson.option.animate=true;
                    var marker=this.map.addPulsingIcon(pulseJson);
                    marker.id=objects[i].id;
                    marker.html="<div>"+objects[i].name+"</div>";
                    var option={};
                    option.offset=[0,0];//弹出偏移
                    this.map.bindPopup(marker,option);
                    window.realtimeArr.push(marker);
                    console.log("波纹点",marker);
                }
            }
        },
        beforeCreate() {
        },
        created() {
        },
        beforeMount() {
        },
        mounted() {

        },
        beforeUpdate() {
        },
        updated() {
        },
        beforeDestroy() {
        },
        destroyed() {
        }
    }
</script>

<style>

</style>
