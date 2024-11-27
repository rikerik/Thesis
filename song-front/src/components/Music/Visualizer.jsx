import React, { useRef, useEffect } from "react";

const Visualizer = ({ audioRef }) => {
  // Ref to store the canvas DOM element
  const canvasRef = useRef(null);

  useEffect(() => {
    const audio = audioRef.current; //Get the audio reference
    const canvas = canvasRef.current; //Get canvas reference
    const canvasCtx = canvas.getContext("2d"); //Get 2d drawing context

    let audioContext;
    let analyzer;
    let dataArray;
    let bufferLength;

    //Draw function
    const drawVisualizer = () => {
      if (!analyzer || !canvasCtx) return; // Exit if analyzer or context is not set

      const WIDTH = canvas.width;
      const HEIGHT = canvas.height;

      //Update the visualizer
      requestAnimationFrame(drawVisualizer);

      //get frequency data from teh analyzer
      analyzer.getByteFrequencyData(dataArray);
      // Clear the canvas for redrawing
      canvasCtx.clearRect(0, 0, WIDTH, HEIGHT);

      const barWidth = (WIDTH / bufferLength) * 2.5; // Width of each bar
      let barHeight;
      let x = 0; // Initial x-coordinate for the first bar

      // Calculate the average loudness across all frequency data points
      const avgLoudness =
        dataArray.reduce((acc, value) => acc + value, 0) / bufferLength;

      // Determine color based on loudness
      const color = getColorBasedOnLoudness(avgLoudness);

      // Loop through each frequency bin and draw the visualizer bars
      for (let i = 0; i < bufferLength; i++) {
        barHeight = dataArray[i] / 2; //height of the bar

        // Set the fill color and draw the bar
        canvasCtx.fillStyle = color;
        canvasCtx.fillRect(x, HEIGHT - barHeight / 2, barWidth, barHeight);
        x += barWidth + 1;
      }
    };

    // Function to determine color based on loudness
    const getColorBasedOnLoudness = (loudness) => {
      // Map loudness to a color range from green (quiet) to red (loud)
      const red = Math.min(255, Math.max(0, loudness * 2));
      const green = Math.max(0, 255 - loudness * 2);
      const blue = 10 * (loudness / 17); // Keep a constant blue value for aesthetics
      return `rgb(${red},${green},${blue})`;
    };

    if (audio) {
      // Create an audio context and connect it to the audio source
      audioContext = new (window.AudioContext || window.webkitAudioContext)();
      const source = audioContext.createMediaElementSource(audio);
      analyzer = audioContext.createAnalyser(); // Create an analyzer node

      // Connect the audio source to the analyzer and then to the destination (speakers)
      source.connect(analyzer);
      analyzer.connect(audioContext.destination);

      // Set the FFT size for frequency analysis and prepare the data array
      analyzer.fftSize = 256;
      bufferLength = analyzer.frequencyBinCount;
      dataArray = new Uint8Array(bufferLength);

      //Drawing the visualizer
      drawVisualizer();
    }
    // Cleanup function to close the audio context when the component unmounts
    return () => {
      if (audioContext) {
        audioContext.close();
      }
    };
  }, [audioRef]);

  return <canvas ref={canvasRef} width="700" height="50"></canvas>;
};

export default Visualizer;
