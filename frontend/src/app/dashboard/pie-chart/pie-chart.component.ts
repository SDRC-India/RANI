import { Component, OnInit, Input, ViewEncapsulation, ElementRef, ViewChild } from "@angular/core";
import { HttpClient } from '@angular/common/http';
import * as d3 from "d3";
// import * as d3plus from "d3plus-text";
declare var $: any;

@Component({
  selector: 'app-pie-chart',
  templateUrl: './pie-chart.component.html',
  styleUrls: ['./pie-chart.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class PieChartComponent implements OnInit {

  @ViewChild('pieChart') private chartContainer: ElementRef;
  @Input() private data: any;

  constructor(private httpClient: HttpClient, private hostRef: ElementRef) { }

  ngOnInit() {
    if (this.data) {
      this.createChart(this.data);
    }
  }

  createChart(data) {
    let mainData = data;
    let legends = data.chartData[0].legends;
    let headerIndicatorName = data.chartData[0].headerIndicatorName
    let chartType = data.chartsAvailable[0];
    let n = legends, length;
    data = data.chartData[0].chartDataValue[0];
    // legend dimensions
    var legendRectSize = 25; // defines the size of the colored squares in legend
    var legendSpacing = 6; // defines spacing between squares
    let el = this.chartContainer.nativeElement;
    let viewportWidth = $(window).width();
    let divWidth = $(this.hostRef.nativeElement).parent().width();
    d3.select(el).selectAll("*").remove();


    let margin = {
      left: 20,
      right: 20,
      top: -20,
      bottom: 20
    }
    // set width, height
    var y = d3.scaleBand().domain(d3.range(10, n)).range([height, -60]),
      width =
        $(this.hostRef.nativeElement).parent().width() - margin.right - margin.left,
      height = ($(this.hostRef.nativeElement).parent().height() - 50);
    margin.left = (($(this.hostRef.nativeElement).parent().width() - width) / 2) - 90;
    margin.right = ($(this.hostRef.nativeElement).parent().width() - width) / 2;
    var radius = Math.min(width, height) / 2;
    var color = d3.scaleOrdinal().range(
      ["#0E9648", "#F3D329", "#F89C5E", "#AE1F25", "#ad4807"]);
    var chartColor = {
      "Enrolled": "#aedc5c",
      "Left Out": "#f07258"
    }
    if (chartType == "donut") {
      var arc = d3.arc()
        .outerRadius(radius).innerRadius(90).cornerRadius(3)
        .padAngle(0.015);
    } else if (chartType == "pie") {
      var arc = d3.arc()
        .outerRadius(radius).innerRadius(0).cornerRadius(3)
        .padAngle(0.015);
    }

    var pie = d3.pie()
      .value(function (d) {
        return parseFloat(d.value);
      }).sort(null);


    var svg = d3
      .select(el)
      .append("svg")
      .attr("id", "chart")
      .attr("width", width + 20)
      .attr("height", function () {
        if (viewportWidth > 1024) {
          return height + 80
        } else if (viewportWidth == 1024) {
          return height + 30
        } else if (viewportWidth < 1000) {
          return height + 40
        }
      })
      .attr('transform', function (data) {
        if (headerIndicatorName == "Referral of Malnourished children" && viewportWidth > 700) {
          return "translate(4.0008," + (margin.top - 270) + ")"
        } else {
          return "translate(4.0008," + margin.top + ")"
        }
      })

      // .attr('transform', 'translate(' + (margin.left+90) + ',' + margin.top + ')')

      .append("g")
      .attr("transform", function () {
        if (viewportWidth > 450) {
          return "translate(" + (width / 2) + "," + (height / 2 + 20) + ")"
        } else {
          return "translate(" + (width / 2 + 20) + "," + (height / 2 + 25) + ")"
        }
      });


    //check for no data availble
    let allNullValues = true;
    for (let j = 0; j < data.length; j++) {
      if (data[j].value != null) {
        allNullValues = false;
        break;
      }
    }
    
    if (allNullValues && (chartType =='donut'&& mainData.align == 'col-md-12')) {
      svg.append("text")
        .attr("transform", "translate(" + ((width / 2) - 470) + ",-35)")
        .attr("x", 0)
        .attr("y", 30)
        .attr("font-size", "25px")
        .style("text-anchor", "middle")
        .text("Data Not Available");
      return;     
    }
    else if (allNullValues) {
      svg.append("text")
        .attr("transform", "translate(" + ((width / 2) - 230) + ",-35)")
        .attr("x", function () {
          if (viewportWidth >=768 && viewportWidth <=1024) {
            return 100
          }
          if (viewportWidth ==767) {
            return -50
          }
          if (viewportWidth <=608 && viewportWidth >380) {
            return 20
          }
          if(viewportWidth<=360){
            return 120
          }else{
            return 0
          }
        })
        .attr("y", 30)
        .attr("font-size", "25px")
        .style("text-anchor", "middle")
        .text("Data Not Available");
      return;
    }
    function pieChart(data) {
      var g = svg.selectAll(
        ".arc").data(
          pie(data))
        .enter()
        .append("g")
        .attr("class", "arc")
        .attr("align", "left");

      g.append("path")
        .attr("d", arc)
        .attr("class", "pie-path")
        .style("cursor", (d) => {
          if (d.value) {
            return 'pointer'
          }
          else {
            return 'default'
          }
        })
        .on("mouseover", function (d, i) {
          showPopover.call(this, d, i)
          d3.select(this).style("opacity", "0.9");
        })
        .on("mouseout", function (d) {
          removePopovers();
          d3.select(this).style("opacity", "1");
        })
        //   .on("click",
        //     click)
        .transition()
        .delay(
          function (d, i) {
            return i * 1;
          })
        .duration(1000)
        .attrTween('d', function (d) {
          var i = d3.interpolate(d.startAngle + 0.1, d.endAngle);
          return function (t) {
            d.endAngle = i(t);
            return arc(d);
          };
        })
        .attr("class", function (d) {
          if (d.endAngle)
            return d.data.label;
          else
            return d.data.label
              + " zeroValue";
        })
        .style("opacity", "1")
        .style("fill", function (d,i) {
          return legends[i].cssClass;
        })
      // .style("fill", function (d) {
      //   return d.data ? d.data.colorCode:'#333';
      // })
      svg.selectAll(".arc").append("text")
        .attr("transform", function (d) {
          return "translate(" + arc.centroid(d) + ")";
        })
        .attr("dx", "-1em")
        .attr("dy", "0.5em")
        .style("color","#fff")
        .text(function (d) {
          if (d.data.value != 0)
            return Math.round(d.data.value) + "%";
        });

      function showPopover(d, i) {
        $(this).popover(
          {
            title: '',
            placement: 'top',
            container: 'body',
            trigger: 'manual',
            html: true,
            animation: false,
            content: function () {
              if (d.data.axis != '' && d.data.denominator != null && d.data.numerator != null && d.data.unit == 'Percentage') {
                return "<div style='color: #495769;'>" + "<b>" + d.data.axis + "</b>" + "</div>" +
                  "<div>" + " Data Value : " + "<span style='color: #495769;font-weight:500;'>" + d.data.value + "%" + "</span>" + "</div>" +
                  "<div>" + "Numerator : " + "<span style='color: #495769;font-weight:500'>" + d.data.numerator + "</span>" + "</div>" +
                  "<div>" + "Denominator : " + "<span style='color: #495769;font-weight:500'>" + d.data.denominator + "</span>" + "</div>";
              } else if (d.data.denominator == null && d.data.numerator == null && d.data.unit == 'Percentage') {
                return "<div style='color: #495769;'>" + "<b>" + d.data.axis + "</b>" + "</div>" +
                  "<div>" + " Data Value : " + "<span style='color: #495769;font-weight:500;'>" + d.data.value + "%" + "</span>" + "</div>";
              } else if (d.data.denominator == null && d.data.numerator != null && d.data.unit == 'Percentage') {
                return "<div style='color: #495769;'>" + "<b>" + d.data.axis + "</b>" + "</div>" +
                  "<div>" + " Data Value : " + "<span style='color: #495769;font-weight:500;'>" + d.data.value + "%" + "</span>" + "</div>" +
                  "<div>" + "Numerator : " + "<span style='color: #495769;font-weight:500'>" + d.data.numerator + "</span>" + "</div>";
              } else if (d.data.denominator != null && d.data.numerator == null && d.data.unit == 'Percentage') {
                return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
                  "<div>" + " Data Value : " + "<span style='color: #495769;font-weight:500;'>" + d.data.value + "%" + "</span>" + "</div>" +
                  "<div>" + "Denominator : " + "<span style='color: #495769;font-weight:500'>" + d.data.denominator + "</span>" + "</div>";
              }
              else {
                return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
                  "<div style='color: #495769;'> Data Value: " + d.data.value + "</div>";
              }
            }
          });
        $(this).popover('show');
      }
      function removePopovers() {
        $('.popover').each(function () {
          $(this).remove();
        });
      }
      $(".percentVal")
        .delay(1500)
        .fadeIn();
    }
    // add legend   
    // var legend = svg.append("g")
    // .attr("class", "legend")
    // .attr("x", width - 350)
    // .attr("y", 125)
    // .attr("height", 100)
    // .attr("width", 200);

    // legend.selectAll('g').data(legends)
    //     .enter()
    //     .append('g')
    //     .each(function(d, i) {
    //       var g = d3.select(this);
    //       g.append("rect")
    //         .attr("x", width - 350)
    //         .attr("y", i*35)
    //         .attr("width", 10)
    //         .attr("height", 10)
    //         .style("fill", function(d){
    //           return d.cssClass
    //         });

    //   g.append("text")
    //     .attr("x", width - 335)
    //     .attr("y", i * 35 + 2)
    //     .attr("height",100)
    //     .attr("width",200)
    //     .style("font-size",13)
    //     .style("fill",'#000')
    //     .style("cursor","pointer")
    //     // .text(d.value)
    //     // .call(wrap, width + margin.left, width);
    //     .each(function (d, i) {
    //       var lines = wordwrap(d.value)
    //       for (var j = 0; j < lines.length; j++) {
    //         d3.select(this).append("tspan")
    //         .style("line-height",1)
    //         .style("width",width/2 - 100)
    //             .attr("dy","8")
    //             .attr("x",function(d,i) { 
    //                return 163; })
    //               .text(lines[j])
    //       }
    //     })

    // });

    function wordwrap(text) {
      var lines = text.split(/\s+/)
      return lines
    }


    pieChart(data);
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
          tspan = text.text(null).append("tspan").attr("x", 0).attr("y", function (d, i) {
            return 0
          }).attr("dy", dy + "em");
        while (word = words.pop()) {
          line.push(word);
          tspan.text(line.join(" "));
          if (tspan.node().getComputedTextLength() > width) {
            line.pop();
            tspan.text(line.join(" "));
            line = [word];
            tspan = text.append("tspan").attr("x", 0).attr("y", function (d, i) {
              return 0
            }).attr("dy", ++lineNumber + dy + "em").text(word);
          }
        }
      });
    }
  }
}
