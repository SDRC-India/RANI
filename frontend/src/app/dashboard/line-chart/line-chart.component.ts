import { Component, OnInit, ViewEncapsulation, OnChanges, ViewChild, ElementRef, Input } from '@angular/core';
import * as d3 from 'd3';

declare var $: any;

@Component({
  selector: 'app-line-chart',
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class LineChartComponent implements OnInit {

  @ViewChild('linechart') private chartContainer: ElementRef;
  @Input() private data: any;
  @Input() private unit: any;

  constructor(private hostRef: ElementRef) { }

  ngOnInit() {
    if (this.data.length > 0) {
      this.createChart(this.data);
    }
  }

  ngOnChanges(changes) {
    if (this.data && changes.data.previousValue) {
      this.createChart(this.data);
    }
  }

  createChart(data) {
    let el = this.chartContainer.nativeElement;
    let viewportWidth = $(window).width();
    d3.select(el).selectAll("*").remove();
    var margin = {
      top: 20,
      right: 20,
      bottom: 30,
      left: 40
    }
    let w = $(this.hostRef.nativeElement).parent().width();
    let h = $(this.hostRef.nativeElement).parent().height() - 40;

    let width = w - margin.left - margin.right
    let height = h - margin.top - margin.bottom;

    var x = d3.scaleBand().range([0, width], 1.0);
    var y = d3.scaleLinear().rangeRound(
      [height, 0]);

    // define the axis
    var xAxis = d3.axisBottom().scale(x).ticks(5);
    var yAxis = d3.axisLeft().scale(y)
      .ticks(5);

    var dataNest = d3.nest().key(function (d) {
      return d.key;
    }).entries(data);

    var max = d3.max(data.map(function (d) {
      return parseFloat(d.value);
    }))
    if (max < 100) {
      max = 100;
    }
    x.domain(data.map(function (d) {
      return d.axis;
    }));
    y.domain([0, max]);

    // Define the line
    var area = d3.area()
      .curve(d3.curveLinear)
      .x(function (d) { return x(d.axis) + width / data.length * dataNest.length / 2; })
      .y0(height)
      .y1(function (d) { return y(d.value); });

    var lineFunctionCardinal = d3.line()
      .defined(function (d) { return d && d.value != null; })
      .x(function (d) {
        return x(d.axis) + width / data.length * dataNest.length / 2;
      }).y(function (d) {
        return y(d.value);
      }).curve(d3.curveLinear);

    // Adds the svg canvas
    var svg = d3.select(el).append("svg").attr("id",
      "trendsvg").attr("width",
        w).attr(
          "height",
          h)
      .append("g").attr(
        "transform",
        "translate(" + margin.left + ","
        + (margin.top) + ")").style(
          "fill", "#000");

    var color = d3.scaleOrdinal().range(
      ["#AE1F25", "#F89C5E", "#F3D329", "#0E9648"]);


    // add the x-axis
    svg.append("g").attr("class", "x axis")
      .attr(
        "transform", "translate(0," + height + ")")
      .call(xAxis)

    d3.selectAll(".x.axis .tick text").attr("dx", "0").attr("dy",
      "10").style({
        "text-anchor":
          "middle", "font-size": "11px", "font-weight": "normal"
      });

    svg.selectAll("text");
    if (this.unit) {
      svg.append("g").attr("class", "y axis").call(yAxis)
        .append("text").attr("transform",
          "rotate(-90)").attr("y", -35).attr("x", -height / 2).attr(
            "dy", ".71em")
        .attr("text-anchor", "end").style("fill", "#333")
        .style("font-weight", "400")
        .attr("font-family", "'Questrial', sans-serif")
        .style("font-size", "13px")
        .text(this.unit);
    } else {
      svg.append("g").attr("class", "y axis").call(yAxis)
        .append("text").attr("transform",
          "rotate(-90)").attr("y", -35).attr("x", -height / 2).attr(
            "dy", ".71em")
        .attr("text-anchor", "end").style("fill", "#333")
        .style("font-weight", "400")
        .attr("font-family", "'Questrial', sans-serif")
        .style("font-size", "13px")
        .text(data[0].unit);
    }


    //check for no data availble
    let allNullValues = true;
    if (data.length > 0) {
      for (let j = 0; j < data.length; j++) {
        if (data[j].value != null) {
          allNullValues = false;
          break;
        }
      }
    }
    if (allNullValues) {
      svg.append("text")
        .attr("transform", "translate(" + width / 2 + ",80)")
        .attr("x", 0)
        .attr("y", 30)
        .attr("font-size", function () {
          if (viewportWidth < 420) {
            return "22px";
          } else {
            return "25px";
          }
        })
        .style("text-anchor", "middle")
        .text("Data Not Available");
      return;
    }

    // adding multiple lines in Line chart
    for (let index = 0; index < dataNest.length; index++) {

      var series = svg.append(
        "g").attr("class", "series tag" + dataNest[index].key.split(" ")[0]).attr("id",
          "tag" + dataNest[index].key.split(" ")[0]);

      // svg.append("path")
      //   .data([dataNest[index].values])
      //   .attr("class", "area")
      //   .attr("d", area)
      //   .style("fill", "#f4A775")
      //   .style("opacity", "0.26")

      var path = svg.selectAll(".series#tag" + dataNest[index].key.split(" ")[0])
        .append("path")
        .attr("class", "line tag" + dataNest[index].key.split(" ")[0])
        .attr("id", "tag" + dataNest[index].key.split(" ")[0])
        .attr(
          "d",
          function (d) {
            return lineFunctionCardinal(dataNest[index].values);
          }).style("stroke", function () {
            // return "red";
            if (dataNest[index].key == 'LATESUBMISSION') {
              return "#689435";
            } else if (dataNest[index].key == 'ONTIMESUBMISSION') {
              return "#1a9641";
            } else if (dataNest[index].key == 'INVALIDSUBMISSION') {
              return "#fdae61";
            } else if (dataNest[index].key == 'Severe') {
              return "#AE1F25";
            } else if (dataNest[index].key == 'Moderate') {
              return "#F89C5E";
            } else if (dataNest[index].key == 'Mild') {
              return "#F3D329";
            } else if (dataNest[index].key == 'Normal') {
              return "#0E9648";
            } else {
              //return color(dataNest[index].key)
              return "#1a9641";
            }

          }).style("stroke-width", "1px").style(
            "fill", "none").style("cursor", function (d) {
              return "default";
            }).on("mouseover",
              function (d) {
                if ($(this).attr("id") == "tagP-Average")
                  showPopover.call(this, dataNest[3].values[0]);
              }).on("mouseout", function (d) {
                removePopovers();
              });;


      var totalLength = path.node().getTotalLength();

      path.attr("stroke-dasharray", totalLength + " " + totalLength)
        .attr("stroke-dashoffset", totalLength)
        .transition()
        .duration(3000)
        //  .ease("linear")
        .attr("stroke-dashoffset", 0);
      let chart = svg.selectAll(".series#tag" + dataNest[index].key.split(" ")[0]).select(".point").data(function () {
        return dataNest[index].values;
      }).enter().append("circle").attr("id",
        "tag" + dataNest[index].key.split(" ")[0])
        .attr("class", function (d) {
          return dataNest[index].key.split(" ")[0]
        }).attr("cx", function (d) {
          return x(d.axis) + width / data.length * dataNest.length / 2;
        }).attr("cy", function (d) {
          return y(d.value);
        }).attr("r", function (d) {
          if (d.value == null)
            return "0px";
          else
            return "3px";
        })
        .style("fill", function () {
          if (dataNest[index].key == 'LATESUBMISSION') {
            return "#689435";
          } else if (dataNest[index].key == 'ONTIMESUBMISSION') {
            return "#1a9641";
          } else if (dataNest[index].key == 'INVALIDSUBMISSION') {
            return "#fdae61";
          } else if (dataNest[index].key == 'Severe') {
            return "#AE1F25";
          } else if (dataNest[index].key == 'Moderate') {
            return "#F89C5E";
          } else if (dataNest[index].key == 'Mild') {
            return "#F3D329";
          } else if (dataNest[index].key == 'Normal') {
            return "#0E9648";
          } else {
            //return color(dataNest[index].key)
            return "#1a9641";
          }
        }).style("stroke", "none").style(
          "stroke-width", "2px").style("cursor", "pointer")
        .on("mouseover", function (d) {
          showPopover.call(this, d);
        }).on("mouseout", function (d) {
          removePopovers();
        });
      
        /** Add circle values as text on each circle */
      svg.selectAll(".series#tag" + dataNest[index].key.split(" ")[0]).select(".point").data(function () {
        return dataNest[index].values;
      }).enter().append("text")
        .attr("class", function (d) {
          return dataNest[index].key.split(" ")[0] + "value"
        }).attr("x", function (d) {
          return x(d.axis) + width / data.length * dataNest.length / 2;
        }).attr("y", function (d) {
          return y(d.value);
        }).attr("font-size", "10px")
        .attr("dx", "-9")
        .attr("dy", "-4")
        //.text(function (d) { return d.value })


      // second render pass for the dashed lines
      var left, right
      for (var j = 0; j < dataNest[index].values.length; j += 1) {
        var current = dataNest[index].values[j]
        if (current.value != null) {
          left = current
        } else {
          // find the next value which is not nan
          while (dataNest[index].values[j] != undefined && dataNest[index].values[j].value == null && j < dataNest[index].values.length) j += 1
          right = dataNest[index].values[j]

          // if (left != undefined && right != undefined && left.key == right.key) {
          //   svg.append("path")
          //     .attr("id", "tag" + dataNest[index].key)
          //     .attr("class", "tag" + dataNest[index].key)
          //     .attr("d", lineFunctionCardinal([left, right]))
          //     .style("stroke", "#f4A775")
          //     .attr('stroke-dasharray', '5, 5').style(
          //       "fill", "none");
          // }
          j -= 1
        }
      }

      // svg.selectAll("g.value")
      //   .data([dataNest[index].values])
      //   .enter().append("g")
      //   .attr("class", "value")
      //   .selectAll("circle")
      //   .data(function (d) {
      //      return d;
      //      })
      //   .enter().append("text")
      //   .attr("x", function (d) { return x(d.axis) + width / data.length * dataNest.length / 2 })
      //   .attr("y", function (d) { return y(d.value) - 10 })
      //   .attr("dx", "-9")
      //   .attr("dy", "0")
      //   .attr("font-size", "13px")
      //   .text(function (d) { return d.value; });
    }

    svg.append("text").attr("x", width / 2)// author
      .attr("y", height + 90).attr("dy", ".3em")
      .text("Time Period")
      .style({
        "fill": "rgb(66, 142, 173)",
        "font-weight": "bold",
        "text-anchor": "middle",
        "font-size": "13px"
      })

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
            return "<div style='color: #495769;'>" + "<b>" + d.axis + "</b>" + "</div>" +
              "<div style='color: #495769;'> Data Value: " + d.value + "</div>";
          }
        });
      $(this).popover('show');
    }

    d3.selection.prototype.moveToFront = function () {
      return this.each(function () {
        this.parentNode.appendChild(this);
      });
    };
    d3.selectAll(".domain, .y.axis .tick line").style({ "fill": "#d85c54", "stroke": "#f4A775" });
    d3.selectAll("circle.point").moveToFront();
    d3.selectAll("circle.point").enter().append("text");
  }
}
