import { Component, OnInit, ElementRef, Input, ViewEncapsulation, ViewChild } from '@angular/core';
import * as d3 from 'd3';
declare var $ :any;



@Component({
  selector: 'app-horizonatl-bar-chart',
  templateUrl: './horizonatl-bar-chart.component.html',
  styleUrls: ['./horizonatl-bar-chart.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class HorizonatlBarChartComponent implements OnInit {

  @ViewChild('horizontalBarChart') private chartContainer: ElementRef;
  @Input()
  data: any;
  tooltipOpen = false;

  constructor(private hostRef: ElementRef) { 
  }

  ngOnInit() {
    if(this.data){
      this.createChart(this.data);
    }
  }

  createChart(data){

    let el = this.chartContainer.nativeElement;
    d3.select(el).select("svg").remove();

    var  margin = {top: 25, right: 20, bottom: 100, left: 80}, width =
        $(this.hostRef.nativeElement).parent().width() - margin.right - margin.left, height = $(this.hostRef.nativeElement).parent().height() - margin.top - margin.bottom;
        var viewportWidth = $(window).width();

    var svg = d3.select(el).append("svg").attr("id",
        "horizontalBar").attr("width",
        width + margin.left + margin.right).attr("height",
          height + margin.top + margin.bottom)
     
    var color = [ "#a10101" ];
    var width = +svg.attr("width") - margin.left - margin.right-70;
    var height = +svg.attr("height") - margin.top - margin.bottom;
    
    var x = d3.scaleLinear().range([0, width-50]);
    var y = d3.scaleBand().domain(d3.range(10, data.length)).range([height, -60]);
    
    var max = d3.max(data.map(function (d) {
      return parseFloat(d.value);
    }));
 
    var g = svg.append("g").attr("class", "layer").style("fill",
    function (d, i) {
      return color[i];
    })
        .attr("transform", "translate(" + ( max >10? margin.left+65 : 150) + "," + (margin.left-5) + ")");
        x.domain([0, max]);
        y.domain(data.map(function(d) { return d.axis; })).padding(0.5)
        ;


    g.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(d3.axisBottom(x).ticks(5))
        .append("text").attr("x",width-50)
        .attr("y","35").attr("dx", "-5em")																			
        .style("fill", "#333")
        .style("font-weight", "400")
        .attr("font-family", "'Questrial', sans-serif")
        .style("font-size", "13px")
        .text(data[0].unit);
 
    g.append("g")
        .attr("class", "y axis")
        .call(d3.axisLeft(y))
        .selectAll("text")
       
        .attr("class", "axis")
        .style("fill", "#000")
        .attr("font-family", "'Questrial', sans-serif")
        .call(wrap, ((margin.bottom-5)+45))
        
      //check for no data availble
      let allNullValues = true;
      for (let j = 0; j < data.length; j++) {
      if (data[j].value != null) {
          allNullValues = false;
          break;
      }
      }
      if (allNullValues) {
      svg.append("text")
          .attr("transform", "translate(" + ((width / 2)+ 100) + ",90)")
          .attr("x", function () {
            if (viewportWidth >=768) {
              return 50
            } if (viewportWidth <=380) {
              return 60
            } else{
              return 0
            }
          })
          .attr("y", 30)
          .attr("font-size", function () {
            if (viewportWidth <=380) {
              return "12px"
            } 
            if (viewportWidth <=768) {
              return "16px"
            } else{
              return "25px"
            }
          })
          .style("text-anchor", "middle")
          .text("Data Not Available");
      return;
      }
      var bars = g.selectAll(".bar")
        .data(data)
        .enter().append("g").attr("class", "bar");
      bars.append("rect")
        .attr("class", "horizontal")
        .attr("x", 1)
        .attr("height", y.bandwidth())
        .attr("y", function(d) { return y(d.axis); })
        .attr("width", 0)
        .style("cursor", (d) => {
          if(d.value){
            return 'pointer'
          }
          else{
            return 'default'
          }
        })
        
        .on("mouseover", function (d) {
          showPopover.call(this, d)
          d3.select(this)
          .attr('fill', "#ce493b");
        }).on("mouseout", function (d) {
          removePopovers()
          d3.select(this).attr("fill", function() {
            return "#a10101";
        });
        });
        
    bars.append("text")
          .attr("class", "label")
          //y position of the label is halfway down the bar
          .attr("x", 5)
          .attr("y", function (d) {
              return y(d.axis) + y.bandwidth() / 2 + 4;
          })
          .attr("transform", (d) => {
            if(d.value == null){
              return "translate(30, 0)"
            }
          })
          .style("fill", (d) => {
              return "#000"
          })
          .style("font-size","12px")
          .text(function(d){
            return d.value;
          });

        bars.selectAll("rect").transition().duration(2000).attr("width", function(d) { return x(d.value); });

        bars.selectAll("text").transition().duration(2000).delay(0)
        .attr("x", function (d) {
          return x(d.value)+ 5;
        })
      .tween("text", function(d) {
      let i = d3.interpolate(0, d.value);
      return function(t) {
        if(d.value > 1){
          d3.select(this).text(Math.round(i(t)) );
        }else{
          d3.select(this).text(i(t));
        }
      };
    });

        function removePopovers() {
          $('.popover').each(function() {
            $(this).remove();
          });
        }
        function showPopover(d) {
          $(this).popover({
            title : '',
            placement : 'top',
            container : 'body',
            trigger : 'manual',
            html : true,
            animation: false,
            content : function() {
              if(d.axis != '' && d.denominator != null && d.numerator!=null && d.unit == 'Percentage'){
                return "<div style='color: #495769;'>" +"<b>"+ d.axis +"</b>"+ "</div>" + 
                "<div>" +" Data Value : "+"<span style='color: #495769;font-weight:500;'>"+ d.value +"%"+"</span>"+ "</div>"+
                "<div>" + "Numerator : " +"<span style='color: #495769;font-weight:500'>"+ d.numerator +"</span>"+ "</div>"+
                "<div>" +"Denominator : " +"<span style='color: #495769;font-weight:500'>"+ d.denominator +"</span>"+ "</div>";
              }else if(d.denominator == null && d.numerator==null && d.unit == 'Percentage'){
                return "<div style='color: #495769;'>" +"<b>"+ d.axis +"</b>"+ "</div>" + 
                "<div>" +" Data Value : "+"<span style='color: #495769;font-weight:500;'>"+ d.value +"%"+"</span>"+ "</div>";
              } else if(d.denominator == null && d.numerator!=null && d.unit == 'Percentage'){
                return "<div style='color: #495769;'>" +"<b>"+ d.axis +"</b>"+ "</div>" + 
                "<div>" +" Data Value : "+"<span style='color: #495769;font-weight:500;'>"+ d.value +"%"+"</span>"+ "</div>"+
                "<div>" + "Numerator : " +"<span style='color: #495769;font-weight:500'>"+ d.numerator +"</span>"+ "</div>";
              }else if(d.denominator != null && d.numerator==null && d.unit == 'Percentage'){
                return "<div style='color: #495769;'>" +"<b>"+ d.axis +"</b>"+ "</div>" + 
                "<div>" +" Data Value : "+"<span style='color: #495769;font-weight:500;'>"+ d.value +"%"+"</span>"+ "</div>"+
                "<div>" +"Denominator : " +"<span style='color: #495769;font-weight:500'>"+ d.denominator +"</span>"+ "</div>";
              }
            else{
                return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
                 "<div style='color: #495769;'> Data Value: " + d.value + "</div>";
            } 
          }
          });
          $(this).popover('show');
          // $('.popover.fade.top.in').css('top', parseFloat($('.popover.fade.top.in').css('top').slice(0, -2))+$(window).scrollTop());
        }
        
    function wrap(text, width) {
      text.each(function() {
        var text = d3.select(this),
            words = text.text().split(/\s+/).reverse(),
            word,
            cnt=0,
            line = [],
            lineNumber = 0,
            lineHeight = 1, 
            y = text.attr("y"),
            dy = parseFloat(text.attr("dy")),
            tspan = text.text(null).append("tspan").attr("x", -8).attr("y",  (++lineNumber * lineHeight+y)-2 ).attr("dy", (dy) + "em").style("font-size", "10px");
       
            while (word = words.pop()) {
          cnt++;
          line.push(word);
          tspan.text(line.join(" "));
          if (tspan.node().getComputedTextLength() > width) {
            line.pop();
            
            tspan.text(line.join(" "));	
            line = [word];
            if(cnt!=1)
            tspan = text.append("tspan").attr("x", -5).attr("y", y -10 ).attr("dy", ++lineNumber * lineHeight + dy + "em").style("font-size", "10px").text(word);
          }
        }
      });
    }
  }

}
