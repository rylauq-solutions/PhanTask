import React, { useRef, useState } from 'react';

const AssignedTasks = () => {
  const [file, setFile] = useState(null);
  const fileInputRef = useRef(null);

  const handleButtonClick = () => {
    if (!file && fileInputRef.current) {
      fileInputRef.current.click();
    }
    // Add your submit logic here
  };

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleReset = () => {
    setFile(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <div className="w-full h-full rounded-xl border-2 border-[#3e1a17] bg-[#ffffff] p-4 shadow-lg shadow-orange-900/20 flex flex-col justify-center transition-all duration-300 hover:shadow-xl hover:shadow-orange-900/30">
      <h2 className="text-lg text-center font-semibold text-[#522320] mb-1 tracking-tight">Pending Tasks</h2>
      <p className="text-xs font-medium text-center text-[#a67c61] mb-3">Submit before due date</p>
      {!file ? (
        <>
          <button
            onClick={handleButtonClick}
            className="w-full py-2 rounded-lg bg-[#801e1e] text-[#fff1f0] font-medium text-sm shadow-md shadow-[#801e1e]/30 hover:bg-[#662924] hover:shadow-lg hover:shadow-[#801e1e]/40 active:bg-[#522320] transition-all duration-200 transform hover:-translate-y-0.5"
          >
            Upload
          </button>
          <input
            type="file"
            ref={fileInputRef}
            className="hidden"
            onChange={handleFileChange}
          />
        </>
      ) : (
        <div className="flex items-center justify-between bg-[#f4e3d2] border-2 border-[#c8a07e] p-2 rounded-lg transition-all duration-200">
          <span className="text-[#522320] text-xs font-medium truncate flex-1">{file.name}</span>
          <div className="flex ml-2 gap-2 flex-shrink-0">
            <button
              onClick={() => alert('Submitted!')} // Replace with real logic
              className="py-1 px-3 rounded-lg bg-[#801e1e] text-[#fff1f0] font-semibold text-xs shadow-md shadow-[#801e1e]/30 hover:bg-[#662924] hover:shadow-lg hover:shadow-[#801e1e]/40 active:bg-[#522320] transition-all duration-200 transform hover:-translate-y-0.5"
            >
              Submit
            </button>
            <button
              aria-label="Reset file"
              onClick={handleReset}
              className="text-[#801e1e] hover:text-[#522320] hover:bg-[#801e1e]/10 text-lg font-bold px-2 py-1 rounded-md focus:outline-none focus:ring-2 focus:ring-[#801e1e]/40 transition-all duration-200"
            >
              &times;
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

export default AssignedTasks;