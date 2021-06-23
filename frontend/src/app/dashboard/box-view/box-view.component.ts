import { Component, OnInit, Input, ViewChild, ElementRef } from '@angular/core';
import * as d3 from 'd3';
import { Router } from '@angular/router';
declare var $: any;

@Component({
  selector: 'app-box-view',
  templateUrl: './box-view.component.html',
  styleUrls: ['./box-view.component.scss']
})
export class BoxViewComponent implements OnInit {
  // @Input() private data: Array<any>;
  @Input('data') private data: Array<any>;
  @ViewChild('boxChart') private chartContainer: ElementRef;
  boxData: any;
  subsectors: any;
  groupIndList: any;
  allNullValues: boolean = true;
  viewportWidth: number;
  constructor(private hostRef: ElementRef,private router: Router) { }

  ngOnInit() {
    this.viewportWidth = $(window).width();
    if (this.data) {
      this.boxData = this.data;
      this.createSvg(this.data)
      // check null value
      for (let s = 0; s < this.data.length; s++) {
        if (this.data[s].value != null) {
          this.allNullValues = false;
          break;
        }
      }
    }
  }

  createSvg(data) {
    let el = this.chartContainer.nativeElement;
    d3.select(el).select("svg").remove();
    var viewportWidth = $(window).width();
    let pageName = this.router.url;
    let lastURLSegment = pageName.substr(pageName.lastIndexOf('/') + 1);
    var n = data.length;
    var layers = data;
   
      layers.forEach(function (el, j) {
        el.y = undefined;
        el.y0 = j;
      });
   
    var formatComma = d3.format(","),
    margin = {top: 25, right: 20, bottom: 100, left: 20},
      width =
        $(this.hostRef.nativeElement).parent().width() - margin.right - margin.left,
      height = ($(this.hostRef.nativeElement).parent().height() - 50);

    var x = d3.scaleLinear().range([0, width-50]);
    var y = d3.scaleBand().domain(d3.range(10, n)).range([height, -60]);

    var svg = d3.select(el).append("svg").attr("id",
      "card").attr("width",
        width + margin.left + margin.right).attr("height",
          height);

    //check for no data availble
    // let allNullValues = true;
    // for (let j = 0; j < data.length; j++) {
    //     if (data[j].value != null) {
    //       allNullValues = false;
    //       break;
    //     }
    // }
  
      var layer = svg.selectAll(".layer").data(layers).enter()
      .append("g").attr("class", "layer").style("fill",
        function (d, i) {
          return "#A0C2BB";
        }).attr("id", function (d, i) {
          return i;
        });
        var g = svg.append("g")
        .attr("transform", "translate(11,11)");
        // .attr("transform", "translate(" + (  margin.left+22) + "," + (margin.left-5) + ")");
        // x.domain([0, 100]);
        // y.domain(data.map(function(d) { return d.axis; })).padding(0.5);

        var bars = g.selectAll(".bar")
        .data(data)
        .enter().append("g").attr("class", "bar");
      
    
      bars.append("rect")
        .attr("class", "bar horizontal-chart-bars")
        .attr("x", 1)
        .attr("height", function(){
          if(lastURLSegment != "snapshot-view"){
          return (y.bandwidth() - 330)
          }else{
            return (y.bandwidth() - 280)
          }
        }) 
        .attr("y", function(d,i) { 
          return (35*i); 
        })
        .attr("width",width)
        .style("fill","#A0C2BB")
    
        // value
        bars.append("text")
        .attr("class", "label")
        .attr("x", 0)
        .attr("y", function (d,i) {
            return (35*i);
        })
        .attr("transform", "translate(" + ((width / 4)-40) + ",20)")
        .style("fill", (d) => {
            return "#000"
        })
        .style("font-size",function(){
          if(viewportWidth > 768){
            return "16px"
          }else{
            return "12px"
          }
        })
        .text(function(d){
          return d.axis;
        });


        bars.append("text")
        .attr("class", "label")
        .attr("x", 4)
        .attr("y", function (d,i) {
            return (35*i);
        })
        .attr("transform", viewportWidth > 768?
        "translate(" + ((width / 2)+ 100) + ",20)":"translate(" + ((width / 2)+ 84) + ",20)")
        .style("fill", (d) => {
            return "#000"
        })
        .style("font-weight", "500")
        .style("font-size","14px")
        .text(function(d){
          if (d.value == null) {
              return ": "+"Data Not Available";
        }else{
          return ": "+d.value;
        }
          
        });
   

  }

}
