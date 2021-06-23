import { Component, OnInit, Input, ElementRef, ViewChild, ViewEncapsulation } from '@angular/core';
import * as d3 from 'd3';

declare var $: any;

@Component({
  selector: 'app-spider-chart',
  templateUrl: './spider-chart.component.html',
  styleUrls: ['./spider-chart.component.scss']
})
export class SpiderChartComponent implements OnInit {

  @ViewChild('chart') private chartContainer: ElementRef;

  //@Input() private data: SpiderChartModel;

  @Input() private data: Array<any>;

  private margin: any = { top: 20, bottom: 20, left: 20, right: 20 };
  private chart: any;
  private width: number;
  private height: number;
  private xScale: any;
  private yScale: any;
  private colors: any;
  private xAxis: any;
  private yAxis: any;
  allNullValues: boolean = true;
  constructor(private hostRef: ElementRef) {

  }

  ngOnInit() {
    if (this.data) {
      this.createChart(this.data);
    }
  }

  // ngOnChanges(changes) {
  //   if (this.data && changes.data.previousValue) {
  //     this.createChart(this.data);
  //   }
  // }

  createChart(d) {
    let element = this.chartContainer.nativeElement;
    let viewportWidth = $(window).width();
    // d3.select(element).select("svg").remove();
    d3.select(element).selectAll("*").remove();
    let divWidth = $(this.hostRef.nativeElement).parent().width();
    var chartData = d;
    // var width = 250;
    // var height = 250;
    let margin = {
      left: 20,
      right: 20,
      top: -20,
      bottom: 20
    }
    let width =
      $(this.hostRef.nativeElement).parent().width() - margin.right - margin.left,
      height = ($(this.hostRef.nativeElement).parent().height() - 50);
    var cfg = {
      radius: 2.9,
      w: 250,
      h: 250,
      factor: 1,
      factorLegend: .85,
      levels: 10,
      labelFactor: 1.25,
      maxValue: 0,
      radians: 2 * Math.PI,
      opacityArea: 0.5,
      ToRight: 5,
      TranslateX: 140,
      TranslateY: 35,
      ExtraWidthX: 100,
      ExtraWidthY: 100,
      color: d3.scaleOrdinal(d3.schemeCategory10)
    };
    /*if ('undefined' !== typeof options) {
      for ( var i in options) {
        if ('undefined' !== typeof options[i]) {
          cfg[i] = options[i];
        }
      }
    }*/
    // cfg.maxValue = Math.max(cfg.maxValue,
    // d3.max(d,
    // function(i) {
    // return d3.max(i.map(function(o) {
    // return o.value;
    // }));
    // }));
    cfg.maxValue = 100;
    var allAxis = (d[0].map(function (i, j) {
      return i.axis;
    }));
    var mouseOutcolor = ["#8FBBD9", "#FFBF87"];
    var hoverColor = ["#1F77B4", "#FF8C26"];

    var total = allAxis.length;
    var radius = cfg.factor
      * Math.min(cfg.w / 2, cfg.h / 2);
    var Format = d3.format('%');

    var g = d3.select(element)
      .append("svg").style("overflow", "visible")
      .attr("width", width)
      .attr("height", cfg.h + cfg.ExtraWidthY)
      .append("g")
      .attr('transform', "translate(" + ((width/2)-100) + "," + cfg.TranslateY + ")")

    // check null value
    for (let s = 0; s < d[0].length; s++) {
      if (d[0][s].value != null) {
        this.allNullValues = false;
        break;
      }
    }
    // var tooltip;

    // Circular segments
    for (var j = 0; j <= cfg.levels - 1; j++) {
      var levelFactor = cfg.factor * radius
        * ((j + 1) / cfg.levels);
      g.selectAll(".levels")
        .data(allAxis)
        .enter()
        .append("svg:line")
        .attr("x1", function (d, i) {
          return levelFactor * (1 - cfg.factor * Math.sin(i * cfg.radians / total));
        })
        .attr("y1", function (d, i) {
          return levelFactor * (1 - cfg.factor * Math.cos(i * cfg.radians / total));
        })
        .attr("x2", function (d, i) {
          return levelFactor * (1 - cfg.factor * Math.sin((i + 1) * cfg.radians / total));
        })
        .attr("y2", function (d, i) {
          return levelFactor * (1 - cfg.factor * Math.cos((i + 1) * cfg.radians / total));
        })
        .attr("class", "line")
        .style("stroke", "#000")
        .style("stroke-opacity", "0.1")
        .style("stroke-width", "1px")
        .style("stroke-dasharray", 0)
        .attr("transform", "translate(" + (cfg.w / 2 - levelFactor) + ", " + (cfg.h / 2 - levelFactor) + ")");
    }

    // Text indicating at what % each level is
    for (var j = 0; j < cfg.levels; j++) {
      var levelFactor = cfg.factor * radius
        * ((j + 1) / cfg.levels);

      if (!this.allNullValues) {
        g.selectAll(".lev els")
          .data([1])
          // dummy data
          .enter()
          .append("svg:text")
          .attr("x", function (d) {
            return levelFactor * (1 - cfg.factor * Math.sin(0));
          })
          .attr("y", function (d) {
            return levelFactor * (1 - cfg.factor * Math.cos(0));
          })
          .attr("class", "legend")
          // .style("font-family", "sans-serif")
          .style("font-size", "10px")
          .attr("transform", "translate(" + (cfg.w / 2 - levelFactor - cfg.ToRight) + ", " + (cfg.h / 2 - levelFactor) + ")")
          .attr("fill", "#333")
          .attr("text-anchor", "end")
          .text(Math.round((j + 1) * 100 / cfg.levels));
      } else {
        g.selectAll(".lev els")
          .data([1])
          // dummy data
          .enter()
          .append("svg:text")
          .attr("x", function (d) {
            return levelFactor * (1 - cfg.factor * Math.sin(0));
          })
          .attr("y", function (d) {
            return levelFactor * (1 - cfg.factor * Math.cos(0));
          })
          .attr("class", "legend")
          .style("font-size", "10px")
          .style("opacity", "0.2")
          .attr("transform", "translate(" + (cfg.w / 2 - levelFactor - cfg.ToRight) + ", " + (cfg.h / 2 - levelFactor) + ")")
          .attr("fill", "#333")
          .attr("text-anchor", "end")
          .text(Math.round((j + 1) * 100 / cfg.levels));
      }
    }

    var series = 0;
    var axis = g.selectAll(".axis").data(allAxis)
      .enter().append("g").attr("class", "axis");

    if (!this.allNullValues) {
      axis.append("line")
        .attr("x1", cfg.w / 2)
        .attr("y1", cfg.h / 2)
        .attr("x2", function (d, i) {
          return cfg.w / 2 * (1 - cfg.factor * Math.sin(i * cfg.radians / total));
        })
        .attr("y2", function (d, i) {
          return cfg.h / 2 * (1 - cfg.factor * Math.cos(i * cfg.radians / total));
        })
        .attr("class", "line")
        .style("stroke", "#000").style("stroke-width", "1px");

      axis.append("text")
        .attr("class", "legend")
        .text(function (d: any) {
          if(viewportWidth > 1000){
            if (d.split(' ').length > 3)
            return d.split(' ').slice(0, 3).join(" ") + " ...";
          else
            return d;
          }else{
            return d.split(' ').slice(0, 1).join(" ") + " ...";
         
          }
         
        })
        // .style("font-family", "sans-serif")
        .style("font-size", "10px")
        .attr("text-anchor", "start")
        .attr("dy", "1.5em")
        .attr("transform", function (d, i) {
          return "translate(0, -10)";
        })
        .attr("x", function (d, i) {
          return cfg.w / 2 * (1 - cfg.factorLegend * Math.sin(i * cfg.radians / total)) - 35 * Math.sin(i
            * cfg.radians / total);
        })
        .attr("y", function (d, i) {
          return cfg.h / 2 * (1 - Math.cos(i * cfg.radians / total)) - 20
            * Math.cos(i * cfg.radians / total);
        });
    } else {
      axis.append("line")
        .attr("x1", cfg.w / 2)
        .attr("y1", cfg.h / 2)
        .attr("x2", function (d, i) {
          return cfg.w / 2 * (1 - cfg.factor * Math.sin(i * cfg.radians / total));
        })
        .attr("y2", function (d, i) {
          return cfg.h / 2 * (1 - cfg.factor * Math.cos(i * cfg.radians / total));
        })
        .attr("class", "line")
        .style("stroke", "#000").style("stroke-width", "1px")
        .style("opacity", "0.2");


      axis.append("text")
        .attr("class", "legend")
        .text(function (d: any) {
          if (d.split(' ').length > 3)
            return d.split(' ').slice(0, 3).join(" ") + " ...";
          else
            return d;
        })
        // .style("font-family", "sans-serif")
        .style("font-size", "10px")
        .attr("text-anchor", "start")
        .attr("dy", "1.5em")
        .attr("transform", function (d, i) {
          return "translate(0, -10)";
        })
        .style("opacity", "0.2")
        .attr("x", function (d, i) {
          return cfg.w / 2 * (1 - cfg.factorLegend * Math.sin(i * cfg.radians / total)) - 35 * Math.sin(i
            * cfg.radians / total);
        })
        .attr("y", function (d, i) {
          return cfg.h / 2 * (1 - Math.cos(i * cfg.radians / total)) - 20
            * Math.cos(i * cfg.radians / total);
        });
    }

    //check for no data availble
    if (this.allNullValues) {
      g.append("text")
        .attr("transform", "translate(" + (cfg.w - 120) + "," + (cfg.h - 150) + ") rotate(0)")
        .attr("x", 0)
        .attr("y", 30)
        .attr("font-size", "25px")
        .style("text-anchor", "middle")
        .text("Data Not Available");
      return;
    }

    d.forEach(function (y, x) {
      var dataValues = [];
      var fillColor;
      g.selectAll(".nodes")
        .data(y, function (j: any, i): any {
          dataValues.push([cfg.w / 2 * (1 - (parseFloat(Math.max(j.value, 0).toString()) / cfg.maxValue)
            * cfg.factor * Math.sin(i * cfg.radians / total)),
          cfg.h / 2 * (1 - (parseFloat(Math.max(j.value, 0).toString()) / cfg.maxValue)
            * cfg.factor * Math.cos(i * cfg.radians / total))]);
          fillColor = '#f4a775';
        });
      dataValues.push(dataValues[0]);
      g.selectAll(".area")
        .data([dataValues])
        .enter()
        .append("polygon")
        .attr("class", "label")
        .attr("class", "radar-chart-serie" + series)
        .style("stroke-width", "0.1px")
        .style("stroke", "#ffe7e6")
        .attr("points", function (d) {
          var str = "";
          for (var pti = 0; pti < d.length; pti++) {
            str = str + d[pti][0] + "," + d[pti][1] + " ";
          }
          return str;
        })

        .style("fill", function () {
          return fillColor;
        })
        .style("fill-opacity", cfg.opacityArea)
        .on('mouseover',
          function (d) {
            var z = "polygon." + d3.select(this).attr("class");
            g.selectAll("polygon")
              .transition().duration(200)
              .style("fill-opacity", 0.7)
            g.selectAll(z)
              .transition().duration(200)
              .style("fill-opacity", .7);
          })
        .on('mouseout',
          function () {
            g.selectAll("polygon")
              .transition().duration(200)
              .style("fill-opacity", cfg.opacityArea);
          });

      series++;
    });
    series = 0;

    d.forEach(function (y, x) {
      var dataValues = [];

      g.selectAll(".nodes")
        .data(y)
        .enter().append("svg:circle")
        .attr("class", "radar-chart-serie" + series)
        .attr('r', cfg.radius)
        .attr("alt", function (j: any) {
          return Math.max(j.value, 0).toString();
        })
        .attr("cx", function (j: any, i) {
          dataValues.push([cfg.w / 2 * (1 - (parseFloat(Math.max(j.value, 0).toString()) / cfg.maxValue) * cfg.factor
            * Math.sin(i * cfg.radians / total)),
          cfg.h / 2 * (1 - (parseFloat(Math.max(j.value, 0).toString()) / cfg.maxValue) * cfg.factor
            * Math.cos(i * cfg.radians / total))]);
          return cfg.w / 2 * (1 - (Math.max(j.value, 0) / cfg.maxValue)
            * cfg.factor * Math.sin(i * cfg.radians / total));
        })
        .attr("cy", function (j: any, i) {
          return cfg.h / 2 * (1 - (Math.max(j.value, 0) / cfg.maxValue)
            * cfg.factor * Math.cos(i * cfg.radians / total));
        })
        .attr("data-id", function (j: any) {
          return j.axis;
        }).attr("ngbPopover", "circle popover")
        .attr("popoverTitle", "Customized popover")
        .style("fill", "#f4A775")
        .style("fill-opacity", .9)
        .style("cursor", "pointer")
        .on('mouseover', function (d) {
          showPopover.call(this, d);
          d3.select(this).attr('fill', function (d, i) {
            return hoverColor[i];
          });
        }, void function (d: any) {
          var newX = parseFloat(d3.select(this).attr('cx')) - 10;
          var newY = parseFloat(d3.select(this).attr('cy')) - 5;
          var z = "polygon." + d3.select(this).attr("class");
          g.selectAll("polygon").transition().duration(200).style("fill-opacity", 0.1);
          g.selectAll(z).transition().duration(200).style("fill-opacity", .7);
        })
        .on('mouseout', function (d) {
          removePopovers();
          d3.select(this).attr('fill', function (d, i) {
            return '#FF0';
          });
        });
      g.selectAll(".labels")
        .data(y).enter().append('text')
        .attr("text-anchor", "start")
        .style('opacity', 0)
        .style('font-size', '12px')
        .attr("x", function (j: any, i) {
          dataValues.push([cfg.w / 2 * (1 - (parseFloat(Math.max(j.value, 0).toString()) / cfg.maxValue) * cfg.factor
            * Math.sin(i * cfg.radians / total)),
          cfg.h / 2 * (1 - (parseFloat(Math.max(j.value, 0).toString()) / cfg.maxValue) * cfg.factor
            * Math.cos(i * cfg.radians / total))]);
          return cfg.w / 2 * (1 - (Math.max(j.value, 0) / cfg.maxValue)
            * cfg.factor * Math.sin(i * cfg.radians / total)) - 35;
        })
        .attr("y", function (j: any, i) {
          return (cfg.h / 2 * (1 - (Math.max(j.value, 0) / cfg.maxValue)
            * cfg.factor * Math.cos(i * cfg.radians / total))) -10;
        })
        .text(function (d) { return d.value  })
        .transition(200)
        .style('opacity', 1);
      series++;
    });


    function removePopovers() {
      $('.popover').each(function () {
        $(this).remove();
      });
    }
    function showPopover(d) {
      $(this).popover({
        title: '',
        placement: 'top',
        container: 'body',
        trigger: 'manual',
        html: true,
        animation: false,
        content: function () {
          if (d.axis != '' && d.denominator != null && d.numerator != null) {
            return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
              "<div>" + " Data Value : " + "<span style='color: #495769;font-weight:500;'>" + d.value + "%"+ "</span>" + "</div>" +
              "<div>" + "Numerator : " + "<span style='color: #495769;font-weight:500'>" + d.numerator + "</span>" + "</div>" +
              "<div>" + "Denominator : " + "<span style='color: #495769;font-weight:500'>" + d.denominator + "</span>" + "</div>";
          } else if (d.denominator == null && d.numerator == null) {
            return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
              "<div>" + " Data Value : " + "<span style='color: #495769;font-weight:500;'>" + d.value +"%"+ "</span>" + "</div>";
          } else if (d.denominator == null && d.numerator != null) {
            return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
              "<div>" + " Data Value : " + "<span style='color: #495769;font-weight:500;'>" + d.value +"%"+ "</span>" + "</div>" +
              "<div>" + "Numerator : " + "<span style='color: #495769;font-weight:500'>" + d.numerator + "</span>" + "</div>";
          } else if (d.denominator != null && d.numerator == null) {
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
      $("body").addClass("popoverOpened");
      // $('.popover.fade.top.in').css('top', parseFloat($('.popover.fade.top.in').css('top').slice(0, -2))+$(window).scrollTop());
    }

  }

  updateChart() {

  }


}
