import { Component, OnInit, ViewChild, ElementRef, Input, ViewEncapsulation } from '@angular/core';
import * as _ from 'lodash';
import * as d3 from 'd3';
declare var $: any;
declare var svgHelp: any;
declare var bar3d: any;


@Component({
  selector: 'app-three-d-bar-chart',
  templateUrl: './three-d-bar-chart.component.html',
  styleUrls: ['./three-d-bar-chart.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ThreeDBarChartComponent implements OnInit {
  @ViewChild('barChart') private chartContainer: ElementRef;
  @Input() private data: Array<any>;
  @Input('xGrid') private xGrid: boolean;
  @Input('yGrid') private yGrid: boolean;
  @Input('values') private values: boolean;

  constructor(private hostRef: ElementRef) { }

  ngOnInit() {
    if (this.data) {
      this.createChart(this.data)
    }
  }


  createChart(data) {
    let el = this.chartContainer.nativeElement;
    d3.select(el).select("svg").remove();

 
    var margin = {
      top: 30,
      right: 20,
      bottom: 40,
      left: 20,
      front: 0,
      back: 0
    },
      width =
        $(this.hostRef.nativeElement).parent().width() - margin.right - margin.left,
      height = $(this.hostRef.nativeElement).parent().height() - margin.top - margin.bottom;
    var depth = 100 - margin.front - margin.back;


    var xScale = d3.scaleBand().range([0, width]).padding(0.5),
      yScale = d3.scaleLinear().rangeRound([height, 0]),
      zScale = d3.scaleOrdinal().domain([0, 1, 2]).range([0, depth], .4);

    var xAxis = d3.axisBottom().scale(xScale);
    var yAxis = d3.axisLeft().scale(yScale).ticks(6);

    var max = d3.max(data[0].map(function (d) {
      return parseFloat(d.value);
    }));
    if (max < 100){
      max = 100
    }

    if(max == undefined){
      max = 100
    }

    var gridY = d3.scaleLinear().domain([0, max]).range(
      [height, 0]);

    var chart = d3.select(el).append("svg").attr("id", "columnbarChart")
    .attr("width",
    width + margin.left + margin.right).attr("height",
      height + margin.top + margin.bottom)
  .append("g")
  .attr("transform", "translate(35," + (data[0][0].axis.length > 20 ? 12 : 15) + ")");

    // add the Y gridlines
    if (!this.yGrid) {
      chart.append("g")
        .attr("class", "grid")
        .attr('opacity', 0.3)
        // .attr("stroke","#ebebeb")
        .call(make_y_gridlines()
          .tickSize(-width).tickFormat(null)
        ).selectAll("text").remove();
    }
    var color = ["#F4A775"];

    var layers = data;
    layers.forEach(function (el, j) {
      el.y = undefined;
      el.y0 = j;
    });

    var layer = chart.selectAll(".layer").data(layers).enter()
      .append("g").attr("class", "layer")
      .style("fill", color[0]);

    data = data[0];
    //d3.tsv('data.tsv', type, function(err, data) {
    //if (err) return;

    xScale.domain(data.map(function (d) {
      return d.axis;
    }));
    yScale.domain([0, max]);
    const xBandwidth = xScale.bandwidth() > 50 * data.length ? 50 * data.length : xScale.bandwidth();
    function x(d) { return xScale(d.axis); }
    function y(d) { return yScale(d.value); }


    var camera = [width / 2, height / 2, -200];
    var barGen = bar3d()
      .camera(camera)
      .x(x)
      .y(y)
      .z(zScale(0))
      // .attr('width', xScale.rangeBand())
      .width(xScale.bandwidth())
      .height(function (d) { return height - y(d); })
      .depth(xScale.bandwidth());

    chart.append('g')
      .attr('class', 'x axis')
      .attr("transform",
      "translate(0," + height + ")")
      .call(xAxis)
      .selectAll("text").style("text-anchor", "middle")
      .attr("class", function (d, i) { return "chartBartext" + i })
      .attr("dx", "-.2em").attr("dy", ".70em")
      .call(wrap, xScale.bandwidth(), width);

    chart.append('g')
      .attr('class', 'y axis')
      .call(yAxis)
      .append('text')
      .attr('transform', svgHelp.rotate(-90))
      .attr("y", -18 - margin.left)
      .attr("x", 0 - (height / 2))
      .attr("dy", "1em")
      .style('text-anchor', 'end')
      .style("fill", "#333")
      .style("font-weight", "400")
      .attr("font-family", "'Questrial', sans-serif")
      .style("font-size", "13px")
      .text(data[0].unit);

 //check for no data availble
 let allNullValues = true;
 for (let j = 0; j < data.length; j++) {
     if (data[j].value != null) {
       allNullValues = false;
       break;
     }
 }
 if (allNullValues) {
  chart.append("text")
       .attr("transform", "translate("+ width/2 +",0)")
       .attr("x", 0)
       .attr("y",30)
       .attr("font-size", "28px")
       .style("text-anchor", "middle")
       .text("Data Not Available");
       return;
 }
    let cubeBar = layer.selectAll('.bar').data(data)
      .enter().append('g')
      .attr('class', 'bar')
      .style("cursor", "pointer")
      .on("mouseover", function (d) {
        if(d.value)
        showPopover.call(this, d)
      }).on("mouseout", function (d) {
        removePopovers()
      })     // sort based on distance from center, so we draw outermost
      // bars first. otherwise, bars drawn later might overlap bars drawn first
      .sort(function (a, b) {
        return Math.abs(x(b) - 450) - Math.abs(x(a) - 450);
      })
      .call(barGen)

    cubeBar.append("text")
      .attr("class", "below")
      .attr(
        "x",
        function (d) {
          return xScale(d.axis) + (xScale.bandwidth() - xBandwidth) / 2 + xBandwidth
            / (2 * data.length) + (xBandwidth / data.length);
        })
      .attr("y", function (d) {
        return yScale(d.value) - 18;
      })
      .attr("dy", "1.2em")
      // .attr("text-anchor", "left")
      .text(function (d) {
        if(d.value)
         return Math.round(d.value); 
        })
      .style("fill", "#000").style("font-size", "12px");

    function removePopovers() {
      $('.popover').each(function () {
        $(this).remove();
      });
    }
    function showPopover(d) {
      $(this).popover(
        {
          title: '',
          placement: 'top',
          container: 'body',
          trigger: 'manual',
          html: true,
          animation: false,
          content: function () {
            if (d.axis != '' && d.denominator != null && d.numerator != null && d.unit == 'Percentage') {
              return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
                "<div>" + " Data Value : " + "<span style='color: #495769;font-weight:500;'>" + d.value + "%"+"</span>" + "</div>" +
                "<div>" + "Numerator : " + "<span style='color: #495769;font-weight:500'>" + d.numerator + "</span>" + "</div>" +
                "<div>" + "Denominator : " + "<span style='color: #495769;font-weight:500'>" + d.denominator + "</span>" + "</div>";
            } else if (d.denominator == null && d.numerator == null && d.unit == 'Percentage') {
              return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
                "<div>" + " Data Value : " + "<span style='color: #495769;font-weight:500;'>" + d.value +"%"+ "</span>" + "</div>";
            } else if (d.denominator == null && d.numerator != null && d.unit == 'Percentage') {
              return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
                "<div>" + " Data Value : " + "<span style='color: #495769;font-weight:500;'>" + d.value +"%"+ "</span>" + "</div>" +
                "<div>" + "Numerator : " + "<span style='color: #495769;font-weight:500'>" + d.numerator + "</span>" + "</div>";
            } else if (d.denominator != null && d.numerator == null && d.unit == 'Percentage') {
              return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
                "<div>" + " Data Value : " + "<span style='color: #495769;font-weight:500;'>" + d.value + "%"+"</span>" + "</div>" +
                "<div>" + "Denominator : " + "<span style='color: #495769;font-weight:500'>" + d.denominator + "</span>" + "</div>";
            }
            else {
              return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
              "<div style='color: #495769;'> Data Value: " + d.value + "</div>";
            }
          }
        });
      $(this).popover('show');


    }
    // gridlines in x axis function
    function make_x_gridlines() {
      return d3.axisBottom(x)
        .ticks(5)
    }



    // gridlines in y axis function
    function make_y_gridlines() {
      return d3.axisLeft(gridY)
        .ticks(5)
    }
    function type(d) {
      d.value = +d.value;
      return d;
    }

    function wrap(text, width, windowWidth) {
      text.each(function () {
        var text = d3.select(this),
          words = text.text().split(/\s+/).reverse(),
          word,
          cnt = 0,
          line = [],
          lineNumber = 0,
          lineHeight = 1,
          y = text.attr("y"),
          dy = parseFloat(text.attr("dy"));
        if (windowWidth > 660)
          var tspan = text.text(null).append("tspan").attr("x", 0).attr("y", y).attr("dy", dy + "em").style('font-size', '10px');
        else
          var tspan = text.text(null).append("tspan").attr("x", 0).attr("y", y).attr("dy", dy + "em").style('font-size', '10px');

        if (words.length == 1) {
          let chars = words.toString().split("");
          chars.splice((chars.length / 2).toFixed(), 0, '-', ' ');
          tspan.text(chars.join(""));
          if (tspan.node().getComputedTextLength() > width) {
            words = chars.join("").split(/\s+/).reverse();
          }
          tspan.text('');
        }
        while (word = words.pop()) {
          cnt++;
          line.push(word);
          tspan.text(line.join(" "));
          if (tspan.node().getComputedTextLength() > width) {
            line.pop();
            tspan.text(line.join(" "));
            line = [word];
            // if(cnt!=1)
            if (width > 660)
              tspan = text.append("tspan").attr("x", 0).attr("y", y).attr("dy", ++lineNumber * lineHeight + dy + "em").style('font-size', '10px').text(word);
            else
              tspan = text.append("tspan").attr("x", 0).attr("y", y).attr("dy", ++lineNumber * lineHeight + dy + "em").style('font-size', '10px').text(word);
          }
        }
      });
    }

  }




}
