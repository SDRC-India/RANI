import { Component, OnInit, Input, ViewChild, ElementRef } from '@angular/core';
import * as d3 from 'd3';
import { Router } from '@angular/router';
declare var $: any;

@Component({
  selector: 'app-card-view',
  templateUrl: './card-view.component.html',
  styleUrls: ['./card-view.component.scss']
})
export class CardViewComponent implements OnInit {
  @Input('data') private data: Array<any>;
  @ViewChild('cardChart') private chartContainer: ElementRef;
  cardDetails: any;
  viewportWidth: number;

  constructor(private hostRef: ElementRef,private router: Router) { }

  ngOnInit() {
    if (this.data) {
      this.cardDetails = this.data;
      this.createSvg(this.data);
    }
    this.viewportWidth = $(window).width();
  }

  createSvg(data) {
    let el = this.chartContainer.nativeElement;
    let pageName = this.router.url;
    let lastURLSegment = pageName.substr(pageName.lastIndexOf('/') + 1);
    d3.select(el).select("svg").remove();
    var formatComma = d3.format(",")
    var margin = {
      top: 30,
      right: 20,
      bottom: 40,
      left: 20
    },
    width =
        $(this.hostRef.nativeElement).parent().width() - margin.right - margin.left,
      height = $(this.hostRef.nativeElement).parent().height() - margin.top - margin.bottom;

    var x = d3.scaleBand().range([0, width]).padding(0.4);

    var svg = d3.select(el).append("svg").attr("id",
      "card").attr("width",
        width + margin.left + margin.right).
        attr("height",function(){
          if(lastURLSegment != "program-dashboard"){
            return (height - 205);
          }else {
            return (height - 145);
          }
        });

    var rect = svg.append('rect')
     
      .attr("width", width + margin.left + margin.right)
      .attr("height",function(){
        if(lastURLSegment != "program-dashboard"){
          return (height - 205);
        }else {
          return (height - 145);
        }
      })
      .attr("fill", "#bb2a07")
    
    var text = svg.append("text")
      .attr("transform", "translate(" + (width / 2) + "," + (height - 260)+")")
      .attr("x", 5)
      .attr("y", function(){
        if(lastURLSegment !="program-dashboard"){
          return 5;
        }else{
          return 60;
        }
      })
      .style("text-anchor", "middle")
      .style("fill", "#fff")
      .style("font-weight", "500")
      .style("opacity","1")
      .attr("font-family", "'Questrial', sans-serif")
      .style("font-size", "25px")
      .text(function(){
        if(data.indicatorValue == null){
          return "NA";
        }else if(data.indicatorValue != null && (data.unit == '' || data.unit == 'Number')){
          return formatComma(data.indicatorValue); 
        }else if(data.indicatorValue != null && data.unit == "Percentage"){
          return data.indicatorValue + "%";
        }else{
          return data.indicatorValue ;
        }
      })

      svg.append("text")
      .attr('class','wrapme')
      .attr("transform", "translate(" + ((width / 2) + 10) + "," + (height - 230)+")")
      .attr("x", function(){
        if(data.indicatorName.length < 75){
          return 5;
        }else{
          return 100;
        }
      })
      .attr("y", function(){
        if(lastURLSegment != "program-dashboard"){
          return 5;
        }else{
          return 55;
        }
      })
      .style("text-anchor", "middle")
      .style("fill", "#fff")
      .style("font-weight", "500")
      .style("opacity","1")
      .attr("lengthAdjust", "spacingAndGlyphs")
      .attr("font-family", "'Questrial', sans-serif")
      .style("font-size", "12px")
      .text(data.indicatorName)
      .call(wrap, width + margin.left, width);


      function wrap(text, width, windowWidth) {
        text.each(function () {
          var text = d3.select(this),
            words = text.text().split(/\s+/).reverse(),
            word,
            cnt = 0,
            line = [],
            textLength = text.node().getComputedTextLength(),
            lineNumber = 0,
            lineHeight = 1,
            y = text.attr("y"),
            dy = 1,
            tspan = text.text(null).append("tspan").attr("x", 0).attr("y", (y - 10)).attr("dy", dy + "em");
            while (word = words.pop()) {
              line.push(word);
              tspan.text(line.join(" "));
              if (tspan.node().getComputedTextLength() > width) {
                line.pop();
                tspan.text(line.join(" "));
                line = [word];
                tspan = text.append("tspan").attr("x", 0).attr("y", (y - 10)).attr("dy", ++lineNumber + dy + "em").text(word);
              }
            }
        });
      }
  }
}
