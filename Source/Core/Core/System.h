// Copyright 2020 Dolphin Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

#pragma once

#include <memory>

class GeometryShaderManager;
class PixelShaderManager;
class SoundStream;
struct Sram;
class VertexShaderManager;

namespace AudioInterface
{
class AudioInterfaceState;
};
namespace CommandProcessor
{
class CommandProcessorManager;
}
namespace CoreTiming
{
class CoreTimingManager;
}
namespace DSP
{
class DSPState;
}
namespace DVDInterface
{
class DVDInterfaceState;
}
namespace DVDThread
{
class DVDThreadState;
}
namespace ExpansionInterface
{
class ExpansionInterfaceState;
};
namespace Fifo
{
class FifoManager;
}
namespace GPFifo
{
class GPFifoManager;
}
namespace IOS::HLE::USB
{
class SkylanderPortal;
};
namespace Memory
{
class MemoryManager;
};
namespace MemoryInterface
{
class MemoryInterfaceState;
};
namespace PixelEngine
{
class PixelEngineManager;
};
namespace ProcessorInterface
{
class ProcessorInterfaceManager;
}
namespace SerialInterface
{
class SerialInterfaceState;
};
namespace VideoInterface
{
class VideoInterfaceState;
};

namespace Core
{
// Central class that encapsulates the running system.
class System
{
public:
  ~System();

  System(const System&) = delete;
  System& operator=(const System&) = delete;

  System(System&&) = delete;
  System& operator=(System&&) = delete;

  // Intermediate instance accessor until global state is eliminated.
  static System& GetInstance()
  {
    static System instance;
    return instance;
  }

  void Initialize();

  bool IsDualCoreMode() const { return m_separate_cpu_and_gpu_threads; }
  bool IsMMUMode() const { return m_mmu_enabled; }
  bool IsPauseOnPanicMode() const { return m_pause_on_panic_enabled; }

  SoundStream* GetSoundStream() const;
  void SetSoundStream(std::unique_ptr<SoundStream> sound_stream);
  bool IsSoundStreamRunning() const;
  void SetSoundStreamRunning(bool running);
  bool IsAudioDumpStarted() const;
  void SetAudioDumpStarted(bool started);

  AudioInterface::AudioInterfaceState& GetAudioInterfaceState() const;
  CoreTiming::CoreTimingManager& GetCoreTiming() const;
  CommandProcessor::CommandProcessorManager& GetCommandProcessor() const;
  DSP::DSPState& GetDSPState() const;
  DVDInterface::DVDInterfaceState& GetDVDInterfaceState() const;
  DVDThread::DVDThreadState& GetDVDThreadState() const;
  ExpansionInterface::ExpansionInterfaceState& GetExpansionInterfaceState() const;
  Fifo::FifoManager& GetFifo() const;
  GeometryShaderManager& GetGeometryShaderManager() const;
  GPFifo::GPFifoManager& GetGPFifo() const;
  IOS::HLE::USB::SkylanderPortal& GetSkylanderPortal() const;
  Memory::MemoryManager& GetMemory() const;
  MemoryInterface::MemoryInterfaceState& GetMemoryInterfaceState() const;
  PixelEngine::PixelEngineManager& GetPixelEngine() const;
  PixelShaderManager& GetPixelShaderManager() const;
  ProcessorInterface::ProcessorInterfaceManager& GetProcessorInterface() const;
  SerialInterface::SerialInterfaceState& GetSerialInterfaceState() const;
  Sram& GetSRAM() const;
  VertexShaderManager& GetVertexShaderManager() const;
  VideoInterface::VideoInterfaceState& GetVideoInterfaceState() const;

private:
  System();

  struct Impl;
  std::unique_ptr<Impl> m_impl;

  bool m_separate_cpu_and_gpu_threads = false;
  bool m_mmu_enabled = false;
  bool m_pause_on_panic_enabled = false;
};
}  // namespace Core
