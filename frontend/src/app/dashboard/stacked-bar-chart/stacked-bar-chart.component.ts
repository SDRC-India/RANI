import { Component, OnInit, ElementRef, Input, ViewChild } from '@angular/core';
import * as d3 from 'd3v4';
declare var $: any;

@Component({
  selector: 'app-stacked-bar-chart',
  templateUrl: './stacked-bar-chart.component.html',
  styleUrls: ['./stacked-bar-chart.component.scss']
})
export class StackedBarChartComponent implements OnInit {
  @ViewChild('stackbarChart') private chartContainer: ElementRef;

  clickedKeys = [];
  @Input('data') private dataToStack
  totalData = [];

  @Input() private keys;
  @Input() private unit;
  reorderColors = [];

  x: any;
  y: any;
  stackedSeries;
  margin;
  width;
  height;
  allNullValues: boolean = true;
   
  constructor(private hostRef: ElementRef) { }

  ngOnInit() {
    console.log(this.dataToStack);
    if (this.dataToStack) {
      // console.log("keys -----" + this.keys);
      // console.log("data -----" + this.dataToStack);
      this.dataToStack.forEach(element => {
        if(element.value != null){
          this.allNullValues = false;
        }
      });
      this.setupChart()
    }
  }

  setupChart() {

    let el = this.chartContainer.nativeElement;
    d3.select(el).select("svg").remove();
    this.margin = {
      top: 20,
      bottom: 30,
      left: 50,
      right: 20,
    };
    let w = $(this.hostRef.nativeElement).parent().width();
    let h = $(this.hostRef.nativeElement).parent().height();


    this.width = w - this.margin.left - this.margin.right;
    this.height = h - this.margin.top - this.margin.bottom - 35;

    let colors =  ["#800101", "#D31818", "#CE493B", "#ad4807", "#f1af62"];
    let colorsOrder = ['#1f4a7c', '#a5b7f3', '#e0e5f7'];

    this.createStack();

    var formatTick = function (d) {
      return d.split(".")[0];
    };
    this.x = d3.scaleBand()
      .domain(this.dataToStack.map(function (d) {
        //let date = new Date(d.date);
        return d.axis;
      }))
      .rangeRound([0, this.width])
      .padding(0.05);

    this.y = d3.scaleLinear()
      .domain([0, d3.max(this.stackedSeries, function (d) {
        return d3.max(d, (d) => {
          return d[1];
        })
      })])
      .range([this.height, 0]);
    let xAxis = d3.axisBottom().scale(this.x).tickFormat(formatTick);
    let svg = d3.select(el).append('svg')
      .attr('class', 'chart')
      .attr('width', w)
      .attr('height', h);

    //background image
    // var defs = svg.append('svg:defs');

    // defs.append("svg:pattern")
    //   .attr("id", "grump_avatar")
    //   .attr("width", 250)
    //   .attr("height", 250)
    //   .attr("patternUnits", "userSpaceOnUse")
    //   .append("svg:image")
    //   .attr("xlink:href", 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAOEAAADhCAMAAAAJbSJIAAAASFBMVEX///+Wlpb7+/ufn5/19fWmpqaamprv7+/Z2dng4ODBwcG3t7fn5+fQ0NCurq7IyMjNzc2ysrLk5OTd3d3x8fGjo6OqqqrExMTDfR+aAAADC0lEQVR4nO3d0Y6iQBSE4SOgIIgIAr7/my7TDa7NuhO96BAq/yTmJKal+G7EEaHMfv6GzI2qc+N2c6Or3EgGN+zkx7F3o7y7UTRu5K1fmvo1aeLG9eJGU7hxL93ox3mNvUuuX5Oz35Jrn3x5n5y78fDJP5vwz7fzHgXAbH7tIdyjcxBz9TGnYOnoY0ofU3hgfgzWrJI98Pw2Of08+RgkA9w7sJAHPmzQBk7JmTrQAAIEuDUwlwderdIGTsmdOtAAagDD/8oA7guYqAMtXX9xoAac1mi/ycxrAAIECDAqMFUHTsnyQAMIECDA2MD2eW5RFHi92KAONIAAAQKMDWzkgcXycxxVoE8GCBAgwKjAuzrwXNpRGzgl9+pAAwgQ4NbAXh2YHa3UBqbumK8NNIAAAQKMDTwtV5+pAg/u+0RtoAEECBBgbODwc1GJMnBKbtWBBhAgQICxgZU8sLPweTmgTwYIECDAqMBaHnhbjhaqwMZdJ6sNNIAAAW4N7NSBl2q5AZ8qsJ0e6kADCBDg1sBMHZgMyx1iVYFTsjzQAAIECDA2MF1uJK4KnJbKAw0gQIAAYwNH67WBQ2+jOtAAAgQIMDawlAfel/P4qkCfDBAgQIBRgYU8sFl3dqkBS1t1dgkCDSBAgFsDL+rAl84uUeD47OzSBRpAgAC3BibqwL+dXarAg7suSBtoAAECBBgb+L/OLhng9ApxoE8GCBAgwKjAVh14zVedXXJAnwwQIECAUYGNPPDZ2aUK9MkAAQIEGBV4lwd+1dm1R2D1TWfXToEGECDArYEfdnbtF5iNH3V27Rg4fNTZtW+gAQQIEGBs4EkdeLBfO7skgAYQIECAsYGDOjBN3nd26QB9MkCAAAFGBf7T2SUHXHd2CQINIECAAGMDb/LAejlaqAJvNnd2CQMNIECAWwM7eeBXnV17BE7J8kADCBDg1sBMHZgM9tAGTsnyQAOoAXz4mPlc6bzzqY9Zbjro92juTlpKMeZNDEFM/fxE+LpH81bH543EXXL4W5c0WNrmHyQnAbB+k/wH1Ayx7Fv/L9QAAAAASUVORK5CYII=')
    //   .attr("width", 250)
    //   .attr("height", 250)
    //   .attr("x", 0)
    //   .attr("y", 0);

    let chart = svg.append('g')
      .classed('graph', true)
      .attr('transform', 'translate(' + this.margin.left + ',' + this.margin.top + ')');

    const layersBarArea = chart.append('g')
      .attr('class', 'layers');

    this.drawChart(layersBarArea, colors);

    chart.append('g')
      .classed('x axis', true)
      .attr("transform",
        "translate(0," + this.height + ")").call(xAxis)
      .selectAll("text").attr("text-anchor", "middle")
      .attr("class", function (d, i) { return "evmbartext" + i })
      .attr("dx", "-.2em").attr("dy", ".70em")
      .call(wrap, this.x.bandwidth(), this.width);

    // chart.append('g')
    //   .classed('y axis', true)
    //   .call(d3.axisLeft(this.y)
    //     .ticks(6));

      chart.append('g')
        .classed("y axis", true)
        .call(d3.axisLeft(this.y)
        .ticks(6)).append("text")
        .attr("transform", "rotate(-90)")
        .attr("y",-45).attr("x", 0 - (this.height / 2))
        .attr("dy", "1em").attr("text-anchor", "end").attr("fill", "#333")
        .attr("font-weight", "400")
        .attr("font-family", "'Questrial', sans-serif")
        .attr("font-size", "13px").text(this.unit);

    //check for no data availble
    
    if (this.allNullValues) {
      chart.append("text")
        .attr("transform", "translate(" + this.width / 2 + ",80)")
        .attr("x", 0)
        .attr("y", 30)
        .attr("font-size", "25px")
        .attr("text-anchor", "middle")
        .text("Data Not Available");
      return;
    }

    //------------------------- Legend ------------------------//

    let legend = d3.select(".legend").append('ul');

    let legendItems = legend.selectAll('li')
      .data(this.keys)
      .enter()
      .append('li')
      .attr('data-key', (d, i) => {
        return d;
      })
      .attr('data-index', (d, i) => {
        return i;
      })
      .attr('data-color', (d, i) => {
        return colors[i];
      })

    legendItems.append('span')
      .attr('class', 'rect')
      .style('background-color', (d, i) => {
        return colors[i];
      });

    legendItems.append('span')
      .attr('class', 'label')
      .html((d) => {
        return d
      });

    //============Text wrap function in x-axis of column chart=====================
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
          var tspan = text.text(null).append("tspan").attr("x", 0).attr("y", y).attr("dy", dy + "em").attr('font-size', '12px');
        else
          var tspan = text.text(null).append("tspan").attr("x", 0).attr("y", y).attr("dy", dy + "em").attr('font-size', '10px');

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
              tspan = text.append("tspan").attr("x", 0).attr("y", y).attr("dy", ++lineNumber * lineHeight + dy + "em").attr('font-size', '12px').text(word);
            else
              tspan = text.append("tspan").attr("x", 0).attr("y", y).attr("dy", ++lineNumber * lineHeight + dy + "em").attr('font-size', '10px').text(word);
          }
        }
      });
    }
  }

  getKeys() {
    return this.keys;
  }

  createStack() {
    let stack = d3.stack()
      .keys(this.getKeys());
    this.stackedSeries = stack(this.dataToStack);
  }

  drawChart(layersBarArea, colors) {

    layersBarArea.selectAll('g.layer').remove();

    let layersBar = layersBarArea.selectAll('.layer').data(this.stackedSeries)
      .enter()
      .append('g')
      .attr('class', 'layer')
      .attr('attr-key', (d, i) => {
        return d.key;
      })
      .style('fill', function (d, i) {
        return colors[i]
      })

    layersBar.selectAll('rect')
      .data((d) => {
        return d
      })
      .enter()
      .append('rect')
      .attr('height', 0)
      .attr("y", this.height)
      .attr('x', (d, i) => {
        return (this.x(d.data.axis) + 15)
      })
      .style("cursor", "pointer")
      .on("mouseover", function (d, i) {
        showPopover.call(this, d, i);
      })
      .on("mouseout", function (d) {
        removePopovers();
      })
      .attr('width', (this.x.bandwidth() - 30))
      .transition()
      .duration(1000)
      .attr('height', (d, i) => {
        return this.y(d[0]) - this.y(d[1]);
      })
      .attr('y', (d) => {
        return this.y(d[1]);
      })
    layersBar.selectAll('val-text')
      .data((d) => {
        return d
      })
      .enter()
      .append('text')
      .attr("class", "val-text")
      .attr('x', (d, i) => {
        return this.x(d.data.axis) + this.x.bandwidth() / 2
      })
      .attr('y', (d) => {
        return this.y(d[1]) + (this.y(d[0]) - this.y(d[1])) / 2 + 5;
      })
      .style("fill", "#FFF")
      .style("text-anchor", "middle")
      .style("font-size", "13px")
      .text(function (d) {
        if (Math.round(d[1] - d[0]) != 0)
          return Math.round(d[1] - d[0]);
      });
      // for (let j = 0; j < this.dataToStack.length; j++) {
      //   for (let k = 0; k < this.dataToStack[j].length; k++) {
      //     if (this.dataToStack[j][k].value != null) {
      //       this.allNullValues = false;
      //     }
      //   }
      // }

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
            if (d.data.axis != "")
              return "<div style='color: #257ab6;'>" + d.data.axis + "</div>" + "Data Value" + " : "
                + (d[1] - d[0]).toFixed(2);
            else
              return "<div style='color: #257ab6;'> Data Value: " + d.value + "%</div>";
          }
        });
      $(this).popover('show');
    }

    function removePopovers() {
      $('.popover').each(function () {
        $(this).remove();
      });
    }
  }
}